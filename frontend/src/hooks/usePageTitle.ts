import { useEffect } from 'react'
import { t } from '../i18n'

export default function usePageTitle(title?: string) {
  useEffect(() => {
    document.title = title ? `${t('label_app_name')} - ${title}` : t('label_app_name')
  }, [title])
}
