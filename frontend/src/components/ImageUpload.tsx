import { useRef } from 'react'

interface ImageUploadProps {
  existingImage?: string | null
  onImageChange: (base64: string | null, mimeType: string) => void
}

export default function ImageUpload({ existingImage, onImageChange }: ImageUploadProps) {
  const inputRef = useRef<HTMLInputElement>(null)

  const handleFile = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return
    const reader = new FileReader()
    reader.onload = () => {
      const result = reader.result as string
      onImageChange(result, file.type)
    }
    reader.readAsDataURL(file)
  }

  const handleRemove = () => {
    onImageChange(null, '')
    if (inputRef.current) inputRef.current.value = ''
  }

  return (
    <div>
      {existingImage ? (
        <div className="d-flex align-items-center gap-2 mb-1">
          <img src={existingImage} alt="" className="img-thumbnail" style={{ maxHeight: 80 }} />
          <button type="button" className="btn btn-sm btn-outline-danger" onClick={handleRemove}>
            &times;
          </button>
        </div>
      ) : (
        <input type="file" ref={inputRef} className="form-control form-control-sm" accept="image/jpeg,image/png,image/jpg"
          onChange={handleFile} />
      )}
    </div>
  )
}
