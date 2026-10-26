import { useEffect, useRef, useState, type ReactNode } from 'react'
import { t } from '../i18n'

export interface ActiveFilter {
  label: string
  onClear: () => void
}

export interface SortOption {
  value: string
  label: string
}

interface FilterPanelProps {
  tabs?: ReactNode
  search?: string
  onSearchChange?: (value: string) => void
  onSearchSubmit?: () => void
  searchPlaceholder?: string
  sortValue?: string
  sortOptions?: SortOption[]
  onSortChange?: (value: string) => void
  filterContent?: ReactNode
  onReset?: () => void
  activeFilters?: ActiveFilter[]
}

export default function FilterPanel({
  tabs,
  search,
  onSearchChange,
  onSearchSubmit,
  searchPlaceholder,
  sortValue,
  sortOptions,
  onSortChange,
  filterContent,
  onReset,
  activeFilters = [],
}: FilterPanelProps) {
  const [filtersOpen, setFiltersOpen] = useState(false)
  const filtersRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!filtersOpen) return
    const handleClick = (event: MouseEvent) => {
      if (filtersRef.current && !filtersRef.current.contains(event.target as Node)) {
        setFiltersOpen(false)
      }
    }
    const handleKey = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setFiltersOpen(false)
    }
    document.addEventListener('mousedown', handleClick)
    document.addEventListener('keydown', handleKey)
    return () => {
      document.removeEventListener('mousedown', handleClick)
      document.removeEventListener('keydown', handleKey)
    }
  }, [filtersOpen])

  const showSearch = onSearchChange !== undefined
  const showFilters = filterContent !== undefined || onReset !== undefined
  const showSort = sortOptions !== undefined && onSortChange !== undefined

  return (
    <>
      {tabs}

      {(showSearch || showSort || showFilters) && (
        <div className="row gy-2 mt-3 align-items-center">
          {showSearch && (
            <div className="col-md-6 d-flex gap-3">
              <input
                type="text"
                className="form-control"
                value={search ?? ''}
                placeholder={searchPlaceholder ?? `${t('label_search')}...`}
                onChange={(event) => onSearchChange?.(event.target.value)}
                onKeyDown={(event) => {
                  if (event.key === 'Enter') {
                    event.preventDefault()
                    onSearchSubmit?.()
                  }
                }}
                aria-label={t('label_search')}
              />
              <button type="button" className="btn btn-primary" onClick={onSearchSubmit} aria-label={t('label_search')}>
                <i className="bi bi-search" />
              </button>
            </div>
          )}

          <div className="col-md-6">
            <div className="row gy-2 justify-content-end">
              {showFilters && (
                <div className="col-md-auto col-sm-6">
                  <div className="dropdown" ref={filtersRef}>
                    <button
                      type="button"
                      className="form-select d-flex align-items-center gap-1"
                      onClick={() => setFiltersOpen((value) => !value)}
                      aria-expanded={filtersOpen}
                      aria-haspopup="true"
                    >
                      <i className="bi bi-filter" />
                      {t('label_filters')}
                    </button>

                    {filtersOpen && (
                      <div className="dropdown-menu dropdown-menu-end show position-absolute" style={{ width: 350 }}>
                        <div className="d-flex justify-content-between px-3 py-2">
                          <span className="fw-bold">{t('label_filters')}</span>
                          <button
                            type="button"
                            className="btn-close"
                            aria-label={t('label_close')}
                            onClick={() => setFiltersOpen(false)}
                          />
                        </div>

                        <div className="dropdown-divider" />
                        <div className="px-4 py-3">{filterContent}</div>
                        <div className="dropdown-divider" />

                        <div className="d-flex justify-content-end px-3 py-2">
                          <button type="button" className="btn btn-light" onClick={onReset}>
                            {t('label_reset')}
                          </button>
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              )}

              {showSort && (
                <div className="col-md-auto col-sm-6">
                  <select
                    className="form-select"
                    value={sortValue}
                    onChange={(event) => onSortChange?.(event.target.value)}
                    aria-label={t('label_sort')}
                  >
                    {sortOptions.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {activeFilters.length > 0 && (
        <div className="row mt-3">
          <div className="col-auto d-flex flex-wrap gap-2">
            {activeFilters.map((filter) => (
              <div key={filter.label} className="d-flex align-items-center border p-2 rounded">
                <span className="me-2">{filter.label}</span>
                <button
                  type="button"
                  className="btn-close btn-close-sm btn-sm"
                  aria-label={`${t('label_clear')}: ${filter.label}`}
                  onClick={filter.onClear}
                />
              </div>
            ))}
          </div>
        </div>
      )}
    </>
  )
}
