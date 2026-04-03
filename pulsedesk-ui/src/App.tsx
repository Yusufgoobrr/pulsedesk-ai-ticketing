import {
  useCallback,
  useEffect,
  useMemo,
  useState,
  type FormEvent,
} from 'react'
import './App.css'

const API_BASE = import.meta.env.VITE_API_BASE ?? ''

/** Keeps the ticket panel within the viewport without scrolling */
const TICKETS_PER_PAGE = 5

type TicketSummary = {
  id: number
  title: string
  category: string
  priority: string
}

/** Matches `TicketResponse` from GET /api/v1/tickets/{id} */
type TicketDetail = {
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
  const res = await fetch(`${API_BASE}/api/v1/tickets?size=100`)
  if (!res.ok) {
    throw new Error(`Failed to load tickets (${res.status})`)
  }
  const data: SpringPage<TicketSummary> = await res.json()
  return data.content ?? []
}

async function fetchTicketDetail(id: number): Promise<TicketDetail> {
  const res = await fetch(`${API_BASE}/api/v1/tickets/${id}`)
  if (!res.ok) {
    throw new Error(`Failed to load ticket (${res.status})`)
  }
  return res.json() as Promise<TicketDetail>
}

async function submitComment(
  text: string,
  sourceChannel: SourceChannel,
): Promise<void> {
  const res = await fetch(`${API_BASE}/api/v1/comments`, {
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
  const [selectedTicketId, setSelectedTicketId] = useState<number | null>(null)
  const [ticketDetail, setTicketDetail] = useState<TicketDetail | null>(null)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState<string | null>(null)

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

  const loadTickets = useCallback(async () => {
    setError(null)
    setLoadingTickets(true)
    try {
      setTickets(await fetchTickets())
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Could not load tickets')
      setTickets([])
    } finally {
      setLoadingTickets(false)
    }
  }, [])

  useEffect(() => {
    void loadTickets()
  }, [loadTickets])

  useEffect(() => {
    if (selectedTicketId == null) {
      setTicketDetail(null)
      setDetailError(null)
      setDetailLoading(false)
      return
    }

    let cancelled = false
    setTicketDetail(null)
    setDetailLoading(true)
    setDetailError(null)
    void fetchTicketDetail(selectedTicketId)
      .then((detail) => {
        if (!cancelled) setTicketDetail(detail)
      })
      .catch((e) => {
        if (!cancelled) {
          setTicketDetail(null)
          setDetailError(
            e instanceof Error ? e.message : 'Could not load ticket details',
          )
        }
      })
      .finally(() => {
        if (!cancelled) setDetailLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [selectedTicketId])

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    const trimmed = comment.trim()
    if (!trimmed) return

    setSubmitMessage(null)
    setError(null)
    setSubmitting(true)
    try {
      await submitComment(trimmed, sourceChannel)
      setComment('')
      setSubmitMessage('Comment submitted. A support ticket may be created after analysis.')
      setTicketPage(0)
      await loadTickets()
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
                {ticketPages.slice.map((t) => (
                  <li key={t.id} className="ticket-list__item">
                    <button
                      type="button"
                      className={`ticket-card${
                        selectedTicketId === t.id ? ' ticket-card--selected' : ''
                      }`}
                      data-category={t.category}
                      aria-pressed={selectedTicketId === t.id}
                      aria-expanded={
                        selectedTicketId === t.id &&
                        (!!ticketDetail || detailLoading || !!detailError)
                      }
                      aria-controls="ticket-summary-panel"
                      onClick={() =>
                        setSelectedTicketId((cur) =>
                          cur === t.id ? null : t.id,
                        )
                      }
                    >
                      <span className="ticket-card__title">{t.title}</span>
                      <span className="ticket-card__meta">
                        <span className="tag">{t.category}</span>
                        <span
                          className={`pill pill--${t.priority.toLowerCase()}`}
                        >
                          {t.priority}
                        </span>
                      </span>
                    </button>
                  </li>
                ))}
              </ul>

              {selectedTicketId != null ? (
                <div
                  id="ticket-summary-panel"
                  className="ticket-detail"
                  role="region"
                  aria-label="Ticket summary"
                  data-category={
                    ticketDetail?.category ??
                    ticketPages.slice.find((x) => x.id === selectedTicketId)
                      ?.category
                  }
                >
                  {detailLoading ? (
                    <p className="muted ticket-detail__status">
                      Loading summary…
                    </p>
                  ) : detailError ? (
                    <p className="feedback feedback--err" role="alert">
                      {detailError}
                    </p>
                  ) : ticketDetail ? (
                    <>
                      <div className="ticket-detail__head">
                        <h3 className="ticket-detail__title">
                          {ticketDetail.title}
                        </h3>
                        <button
                          type="button"
                          className="btn btn--ghost btn--compact"
                          onClick={() => setSelectedTicketId(null)}
                        >
                          Close
                        </button>
                      </div>
                      <p className="ticket-detail__summary">
                        {ticketDetail.summary}
                      </p>
                    </>
                  ) : null}
                </div>
              ) : null}
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
