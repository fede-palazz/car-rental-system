"use client";

import {
  ColumnDef,
  flexRender,
  getCoreRowModel,
  RowSelectionState,
  useReactTable,
} from "@tanstack/react-table";

import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useState } from "react";
import { Vehicle } from "@/models/Vehicle";

interface DataTableProps<TData, TValue> {
  columns: ColumnDef<TData, TValue>[];
  data: TData[];
  enableRowSelection?: boolean;
  enableMultiRowSelection?: boolean;
  onRowSelection?: (selectedId: number | undefined) => void;
  getRowId?: (row: TData) => string;
}

export function DataTable<TData, TValue>({
  columns,
  data,
  enableRowSelection = false,
  enableMultiRowSelection = false,
  onRowSelection = undefined,
  getRowId = undefined,
}: DataTableProps<TData, TValue>) {
  const [rowSelection, setRowSelection] = useState<RowSelectionState>({});

  const table = useReactTable({
    data,
    columns,
    state: { rowSelection },
    getCoreRowModel: getCoreRowModel(),
    manualFiltering: true,
    getRowId: getRowId ? (row) => getRowId(row) : undefined,
    manualSorting: true,
    manualPagination: true,
    enableRowSelection: enableRowSelection,
    enableMultiRowSelection: enableMultiRowSelection,
    onRowSelectionChange: (updater) => {
      if (onRowSelection && enableRowSelection) {
        const selectedRows =
          typeof updater === "function"
            ? updater(table.getState().rowSelection)
            : updater;
        setRowSelection(selectedRows);

        // Get selected row IDs from the updater value
        const selectedRowIds = Object.keys(selectedRows).filter(
          (key) => selectedRows[key]
        );

        if (selectedRowIds.length > 0) {
          // Use the first selected row's id, or adapt as needed for multi-selection
          const selectedId = selectedRowIds[0];
          onRowSelection(Number(selectedId));
        } else {
          onRowSelection(undefined);
        }
      }
    },
  });

  return (
    <div className="rounded-md border">
      <Table>
        <TableHeader>
          {table.getHeaderGroups().map((headerGroup) => (
            <TableRow key={headerGroup.id}>
              {headerGroup.headers.map((header) => {
                return (
                  <TableHead key={header.id}>
                    {header.isPlaceholder
                      ? null
                      : flexRender(
                          header.column.columnDef.header,
                          header.getContext()
                        )}
                  </TableHead>
                );
              })}
            </TableRow>
          ))}
        </TableHeader>
        <TableBody>
          {table.getRowModel().rows?.length ? (
            table.getRowModel().rows.map((row) => (
              <TableRow
                onClick={row.getToggleSelectedHandler()}
                className={row.getIsSelected() ? "bg-red-500" : ""}
                key={row.id}
                data-state={row.getIsSelected() && "selected"}>
                {row.getVisibleCells().map((cell) => (
                  <TableCell key={cell.id}>
                    {flexRender(cell.column.columnDef.cell, cell.getContext())}
                  </TableCell>
                ))}
              </TableRow>
            ))
          ) : (
            <TableRow>
              <TableCell colSpan={columns.length} className="h-24 text-center">
                No results.
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </div>
  );
}
