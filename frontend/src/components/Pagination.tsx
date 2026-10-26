import { t } from '../i18n'

interface PaginationProps {
  page: number
  totalPages: number
  onPageChange: (page: number) => void
}

export default function Pagination({ page, totalPages, onPageChange }: PaginationProps) {
  if (totalPages <= 1) return null

  const isFirst = page <= 0
  const isLast = page >= totalPages - 1

  return (
    <div className="row align-items-center mt-3">
      <div className="col">
        <span className="text-muted">{t('label_page_xxx_of_yyy', [page + 1, totalPages])}</span>
      </div>

      <div className="col-auto ms-auto">
        <div className="d-flex gap-2">
          <button
            type="button"
            className="btn btn-light"
            disabled={isFirst}
            onClick={() => onPageChange(page - 1)}
            aria-label={t('label_previous')}
          >
            <i className="fa-solid fa-chevron-left px-2" />
          </button>
          <button
            type="button"
            className="btn btn-light"
            disabled={isLast}
            onClick={() => onPageChange(page + 1)}
            aria-label={t('label_next')}
          >
            <i className="fa-solid fa-chevron-right px-2" />
          </button>
        </div>
      </div>
    </div>
  )
}
