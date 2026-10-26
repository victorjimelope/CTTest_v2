import { Link } from 'react-router-dom'
import type { ReactNode } from 'react'
import usePageTitle from '../hooks/usePageTitle'

export interface BreadcrumbItem {
  label: string
  to?: string
}

interface PageHeaderProps {
  title: string
  breadcrumbs?: BreadcrumbItem[]
  action?: ReactNode
}

export default function PageHeader({ title, breadcrumbs = [], action }: PageHeaderProps) {
  usePageTitle(title)

  return (
    <>
      <div className="row mb-3">
        <div className="col h4 text-secondary">
          <Link to="/dashboard" className="text-secondary text-decoration-none" aria-label="Inicio">
            <i className="bi bi-house-fill" />
          </Link>
          {breadcrumbs.map((crumb, index) => (
            <span key={`${crumb.label}-${index}`}>
              {' / '}
              {crumb.to ? (
                <Link to={crumb.to} className="text-secondary text-decoration-none">
                  {crumb.label}
                </Link>
              ) : (
                crumb.label
              )}
            </span>
          ))}
          {breadcrumbs.length === 0 && <span>{` / ${title}`}</span>}
        </div>
      </div>

      <div className="row mt-3 mb-3 align-items-center">
        <div className="col">
          <h2 className="mb-0">{title}</h2>
        </div>
        {action && (
          <div className="col d-flex align-items-center justify-content-end">
            {action}
          </div>
        )}
      </div>
    </>
  )
}
