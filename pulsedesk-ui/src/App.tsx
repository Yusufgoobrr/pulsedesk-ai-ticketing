import {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
  type FormEvent,
} from 'react'
import './App.css'

const API_BASE = (import.meta.env.VITE_API_BASE as string | undefined)?.replace(
  /\/$/,
  '',
) ?? ''

function apiUrl(path: string): string {
  const p = path.startsWith('/') ? path : `/${path}`
  if (API_BASE === '') return p
  return `${API_BASE}${p}`
}

/** Keeps the ticket panel within the viewport without scrolling */
const TICKETS_PER_PAGE = 5

/** Second tickets pull after comment — catches tickets that appear slightly after POST */
const TICKETS_REFRESH_AFTER_COMMENT_MS = 10_000

type TicketSummary = {
  id: number
  title: string
  category: string
  priority: string
  summary: string
}

type SpringPage<T> = {
  content: T[]
}

/** Matches `CommentSourceChannel` on the API */
type SourceChannel = 'APP_REVIEW' | 'WEB_FORM' | 'CHAT_WIDGET' | 'OTHER'

const SOURCE_OPTIONS: { value: SourceChannel; label: string }[] = [
  { value: 'WEB_FORM', label: 'Web form' },
  { value: 'APP_REVIEW', label: 'App store review' },
  { value: 'CHAT_WIDGET', label: 'Chat widget' },
  { value: 'OTHER', label: 'Other' },
]

async function fetchTickets(): Promise<TicketSummary[]> {
  const res = await fetch(apiUrl('/api/v1/tickets?size=100'), {
    cache: 'no-store',
  })
  if (!res.ok) {
    throw new Error(`Failed to load tickets (${res.status})`)
  }
  const data: SpringPage<TicketSummary> = await res.json()
  return data.content ?? []
}

async function submitComment(
  text: string,
  sourceChannel: SourceChannel,
): Promise<void> {
  const res = await fetch(apiUrl('/api/v1/comments'), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      comment: text,
      sourceChannel,
    }),
  })
  if (!res.ok) {
    throw new Error(`Failed to submit comment (${res.status})`)
  }
  await res.text()
}

function App() {
  const [comment, setComment] = useState('')
  const [sourceChannel, setSourceChannel] = useState<SourceChannel>('WEB_FORM')
  const [tickets, setTickets] = useState<TicketSummary[]>([])
  const [loadingTickets, setLoadingTickets] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [submitMessage, setSubmitMessage] = useState<string | null>(null)
  const [ticketPage, setTicketPage] = useState(0)

  /** Bumped only when scheduling a new post-submit refresh; never on effect cleanup (avoids Strict Mode breaking timers). */
  const postCommentRefreshIdRef = useRef(0)
  const postCommentRefreshTimeoutRef = useRef<ReturnType<
    typeof setTimeout
  > | null>(null)
  const loadTicketsRef = useRef<
    (opts?: { silent?: boolean }) => Promise<void>
  >(async () => {})

  const ticketPages = useMemo(() => {
    const pages = Math.max(1, Math.ceil(tickets.length / TICKETS_PER_PAGE))
    const safe = Math.min(ticketPage, pages - 1)
    const slice = tickets.slice(
      safe * TICKETS_PER_PAGE,
      safe * TICKETS_PER_PAGE + TICKETS_PER_PAGE,
    )
    return { pages, safe, slice }
  }, [tickets, ticketPage])

  useEffect(() => {
    const maxPage = Math.max(0, Math.ceil(tickets.length / TICKETS_PER_PAGE) - 1)
    setTicketPage((p) => Math.min(p, maxPage))
  }, [tickets.length])

  const loadTickets = useCallback(async (opts?: { silent?: boolean }) => {
    const silent = opts?.silent === true
    if (!silent) {
      setError(null)
      setLoadingTickets(true)
    }
    try {
      setTickets(await fetchTickets())
      if (silent) setError(null)
    } catch (e) {
      const msg = e instanceof Error ? e.message : 'Could not load tickets'
      if (!silent) {
        setError(msg)
        setTickets([])
      } else {
        setError(msg)
      }
    } finally {
      if (!silent) setLoadingTickets(false)
    }
  }, [])

  loadTicketsRef.current = loadTickets

  useEffect(() => {
    void loadTickets()
  }, [loadTickets])

  useEffect(() => {
    return () => {
      if (postCommentRefreshTimeoutRef.current != null) {
        clearTimeout(postCommentRefreshTimeoutRef.current)
        postCommentRefreshTimeoutRef.current = null
      }
    }
  }, [])

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    const trimmed = comment.trim()
    if (!trimmed) return

    setSubmitMessage(null)
    setError(null)
    setSubmitting(true)

    try {
      if (postCommentRefreshTimeoutRef.current != null) {
        clearTimeout(postCommentRefreshTimeoutRef.current)
        postCommentRefreshTimeoutRef.current = null
      }

      await submitComment(trimmed, sourceChannel)
      setComment('')
      setTicketPage(0)
      setSubmitMessage('Comment submitted')

      const refreshRunId = ++postCommentRefreshIdRef.current

      await loadTicketsRef.current({ silent: true })

      postCommentRefreshTimeoutRef.current = globalThis.setTimeout(() => {
        postCommentRefreshTimeoutRef.current = null
        if (postCommentRefreshIdRef.current !== refreshRunId) return
        void loadTicketsRef.current({ silent: true })
      }, TICKETS_REFRESH_AFTER_COMMENT_MS)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Submit failed')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="desk">
      <header className="desk__header">
        <div className="desk__brand">
          <h1 className="desk__title">PulseDesk</h1>
        </div>
      </header>

      <div className="desk__grid">
      <section className="panel panel--form" aria-labelledby="comment-heading">
        <h2 id="comment-heading" className="panel__title">
          New comment
        </h2>
        <form className="form" onSubmit={handleSubmit}>
          <label htmlFor="source" className="form__label">
            Comment source
          </label>
          <select
            id="source"
            className="form__select"
            value={sourceChannel}
            onChange={(e) =>
              setSourceChannel(e.target.value as SourceChannel)
            }
            disabled={submitting}
          >
            {SOURCE_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>

          <label htmlFor="comment" className="form__label form__label--spaced">
            Comment
          </label>
          <textarea
            id="comment"
            className="form__textarea"
            rows={2}
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            placeholder="Describe the issue or request…"
            disabled={submitting}
          />
          <div className="form__actions">
            <button
              type="submit"
              className="btn btn--primary"
              disabled={submitting || !comment.trim()}
            >
              {submitting ? 'Submitting…' : 'Submit'}
            </button>
          </div>
        </form>
        {submitMessage ? (
          <p className="feedback feedback--ok" role="status">
            {submitMessage}
          </p>
        ) : null}
      </section>

      <section className="panel panel--tickets" aria-labelledby="tickets-heading">
        <div className="panel__head">
          <h2 id="tickets-heading" className="panel__title">
            Support tickets
          </h2>
          <button
            type="button"
            className="btn btn--ghost"
            onClick={() => void loadTickets()}
            disabled={loadingTickets}
          >
            Refresh
          </button>
        </div>

        <div className="ticket-list-wrap">
          {error ? (
            <p className="feedback feedback--err" role="alert">
              {error}
            </p>
          ) : null}

          {loadingTickets ? (
            <p className="muted">Loading tickets…</p>
          ) : tickets.length === 0 ? (
            <p className="muted">No tickets yet.</p>
          ) : (
            <>
              <ul className="ticket-list">
                {ticketPages.slice.map((t) => {
                  const summaryText = (t.summary ?? '').trim()
                  const hasSummary = summaryText !== ''
                  return (
                  <li key={t.id} className="ticket-list__item">
                    <article
                      className="ticket-card"
                      data-category={t.category}
                      aria-labelledby={`ticket-${t.id}-title`}
                      aria-describedby={`ticket-${t.id}-summary`}
                    >
                      <div className="ticket-card__head">
                        <h3
                          className="ticket-card__title"
                          id={`ticket-${t.id}-title`}
                        >
                          {t.title}
                        </h3>
                        <div className="ticket-card__meta">
                          <span className="tag">{t.category}</span>
                          <span
                            className={`pill pill--${t.priority.toLowerCase()}`}
                          >
                            {t.priority}
                          </span>
                        </div>
                      </div>
                      <div className="ticket-card__summary-block">
                        <div
                          className="ticket-card__summary-heading"
                          id={`ticket-${t.id}-summary-h`}
                        >
                          Summary
                        </div>
                        <p
                          className={`ticket-card__summary${hasSummary ? '' : ' ticket-card__summary--empty'}`}
                          id={`ticket-${t.id}-summary`}
                          aria-labelledby={`ticket-${t.id}-summary-h`}
                        >
                          {hasSummary ? summaryText : 'No summary yet.'}
                        </p>
                      </div>
                    </article>
                  </li>
                  )
                })}
              </ul>
            </>
          )}
        </div>

        {tickets.length > TICKETS_PER_PAGE ? (
          <nav
            className="ticket-pager"
            aria-label="Ticket list pages"
          >
            <button
              type="button"
              className="btn btn--ghost btn--compact"
              disabled={ticketPages.safe === 0}
              onClick={() => setTicketPage((p) => Math.max(0, p - 1))}
            >
              Previous
            </button>
            <span className="ticket-pager__status">
              {ticketPages.safe + 1} / {ticketPages.pages}
            </span>
            <button
              type="button"
              className="btn btn--ghost btn--compact"
              disabled={ticketPages.safe >= ticketPages.pages - 1}
              onClick={() => setTicketPage((p) => p + 1)}
            >
              Next
            </button>
          </nav>
        ) : null}
      </section>
      </div>
    </div>
  )
}

export default App
