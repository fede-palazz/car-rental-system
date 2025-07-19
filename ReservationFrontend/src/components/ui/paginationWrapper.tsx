import React from "react";
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationPrevious,
  PaginationNext,
  PaginationEllipsis,
} from "@/components/ui/pagination";

interface PaginationWrapperProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

const PaginationWrapper: React.FC<PaginationWrapperProps> = ({
  currentPage,
  totalPages,
  onPageChange,
}) => {
  const handlePageChange = (page: number) => {
    if (page >= 0 && page < totalPages) {
      onPageChange(page);
    }
  };

  return (
    <Pagination>
      <PaginationContent>
        {/* Previous */}
        <PaginationItem>
          <PaginationPrevious
            aria-label="Previous page"
            onClick={(e) => {
              e.preventDefault();
              handlePageChange(currentPage - 1);
            }}
            className={
              currentPage === 0
                ? "pointer-events-none cursor-not-allowed opacity-50"
                : ""
            }
          />
        </PaginationItem>
        <PaginationItem>
          {currentPage > 0 && (
            <PaginationLink onClick={(e) => e.preventDefault()}>
              {currentPage}
            </PaginationLink>
          )}
        </PaginationItem>
        <PaginationItem>
          <PaginationLink isActive onClick={(e) => e.preventDefault()}>
            {currentPage + 1}
          </PaginationLink>
        </PaginationItem>
        <PaginationItem>
          {currentPage + 1 < totalPages && (
            <PaginationLink onClick={(e) => e.preventDefault()}>
              {currentPage + 2}
            </PaginationLink>
          )}
        </PaginationItem>
        {currentPage + 2 < totalPages && (
          <PaginationItem>
            <PaginationEllipsis />
          </PaginationItem>
        )}
        {/* Next */}
        <PaginationItem>
          <PaginationNext
            aria-label="Next page"
            onClick={(e) => {
              e.preventDefault();
              handlePageChange(currentPage + 1);
            }}
            className={
              currentPage === totalPages - 1
                ? "pointer-events-none cursor-not-allowed opacity-50"
                : ""
            }
          />
        </PaginationItem>
      </PaginationContent>
    </Pagination>
  );
};

export default PaginationWrapper;
