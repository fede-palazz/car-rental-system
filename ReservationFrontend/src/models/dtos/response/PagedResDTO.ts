export interface PagedResDTO<T> {
  currentPage: number;
  totalPages: number;
  totalElements: number;
  elementsInPage: number;
  content: T[];
}
