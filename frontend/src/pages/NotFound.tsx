import { Link } from 'react-router-dom'
import { t } from '../i18n'
import usePageTitle from '../hooks/usePageTitle'

export default function NotFound() {
  usePageTitle(t('label_page_not_found'))

  return (
    <div className="min-vh-100 d-flex align-items-center justify-content-center bg-light">
      <div className="text-center">
        <h1 className="display-1 fw-bold text-secondary">404</h1>
        <h4 className="mb-3">{t('label_page_not_found')}</h4>
        <p className="text-muted mb-4">{t('label_page_not_found_description')}</p>
        <Link to="/" className="btn btn-primary">
          {t('label_go_to_dashboard')}
        </Link>
      </div>
    </div>
  )
}
