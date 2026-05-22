export interface Center {
  id: number
  addDate: string | null
  addUser: number | null
  modDate: string | null
  modUser: number | null
  name: string
}

export interface User {
  id: number
  username: string
  admin: boolean
  center: Center | null
  studentGroups: StudentGroup[]
  studentGroupsAsString: string | null
}

export interface StudentGroup {
  id: number
  center: Center
  name: string
}

export interface Student {
  id: number
  studentGroup: StudentGroup
  identifier: number
  name: string
  birthDate: string | null
  gender: 'UNKNOWN' | 'MALE' | 'FEMALE' | 'NOT_APPLICABLE'
}

export interface Image {
  id: number
  imageData: string
  mimeType: 'JPG' | 'JPEG' | 'PNG'
}

export interface Answer {
  id: number
  order: number
  image: Image | null
  correct: boolean
  countTotal: number
}

export interface Question {
  id: number
  order: number
  question: string
  image: Image | null
  answersLayout: 'COLUMNS' | 'ROWS' | 'GRID'
  answers: Answer[]
  countTotal: number
  countCorrect: number
  countWrong: number
}

export interface Test {
  id: number
  centerId: number
  name: string
  description: string
  type: 'PUBLIC' | 'PRIVATE'
  status: 'DRAFT' | 'ACTIVE' | 'LOCKED'
  questions: Question[]
}

export interface TestInstance {
  id: number
  testId: number
  studentGroup: StudentGroup
  status: 'IN_PROGRESS' | 'CLOSED'
  assignedBy: User
  assignedDate: string
  pin: number
}

export interface StudentTest {
  id: number
  testInstanceId: number
  student: Student
  status: 'ASSIGNED' | 'IN_PROGRESS' | 'FINISHED' | 'NOT_DONE'
  startDate: string | null
  finishDate: string | null
  score: number | null
  comment: string | null
}

export interface LoginResponse {
  token: string
  type: string
  userId: number
  username: string
  admin: boolean
  refreshToken: string
}

export interface AnswerDataDTO {
  id: number
  order: number
  imageData: string | null
}

export interface QuestionDataDTO {
  id: number
  order: number
  question: string
  imageData: string | null
  answersLayout: string
  answers: AnswerDataDTO[]
}

export interface TestDataDTO {
  testId: number
  testName: string
  questions: QuestionDataDTO[]
  totalQuestions: number
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  first: boolean
  last: boolean
  empty: boolean
}
