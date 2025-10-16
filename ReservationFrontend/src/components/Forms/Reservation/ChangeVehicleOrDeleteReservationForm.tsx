import VehicleAPI from "@/API/VehiclesAPI";
import { DataTable } from "@/components/ui/data-table";
import { FormField, FormItem, FormMessage } from "@/components/ui/form";
import PaginationWrapper from "@/components/ui/paginationWrapper";
import VehicleDetailsList from "@/components/VehicleDetailsList";
import { PagedResDTO } from "@/models/dtos/response/PagedResDTO";
import { Vehicle } from "@/models/Vehicle";
import { ColumnDef } from "@tanstack/react-table";
import { useEffect, useState } from "react";
import { Control } from "react-hook-form";

function ChangeVehicleOrDeleteReservationForm({
  control,
  currentVehicleId,
  onVehicleSelection,
  carModelId,
  desiredStartDate,
  desiredEndDate,
}: {
  control: Control;
  currentVehicleId: number;
  onVehicleSelection: (newVehicleId: number | undefined) => void;
  carModelId: number;
  desiredStartDate: Date;
  desiredEndDate: Date;
}) {
  const [currentVehicle, setCurrentVehicle] = useState<Vehicle | undefined>(
    undefined
  );
  const [availableVehicles, setAvailableVehicles] = useState<
    Vehicle[] | undefined
  >(undefined);
  const [order, setOrder] = useState<string>("asc");
  const [sort, setSort] = useState<string>("vin");
  const [pageSize, setPageSize] = useState<number>(9);
  const [page, setPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(1);

  const fetchAvailableVehicles = (
    orderParam: string = order,
    sortParam: string = sort,
    pageParam: number = page,
    pageSizeParam: number = pageSize
  ) => {
    VehicleAPI.getAvailableVehicles(
      carModelId,
      desiredStartDate,
      desiredEndDate,
      orderParam,
      sortParam,
      pageParam,
      pageSizeParam
    )
      .then((vehicles: PagedResDTO<Vehicle>) => {
        setAvailableVehicles(vehicles.content);
        setPage(vehicles.currentPage);
        setTotalPages(vehicles.totalPages);
        //setPageSize(vehicles.elementsInPage);
      })
      .catch((err) => {
        console.log(err);
      });
  };

  const fetchCurrentVehicle = (currentVehicleId: number) => {
    VehicleAPI.getVehicleById(Number(currentVehicleId))
      .then((vehicle: Vehicle) => {
        setCurrentVehicle(vehicle);
      })
      .catch((err) => {
        console.log(err);
      });
  };

  useEffect(() => {
    fetchAvailableVehicles();
    fetchCurrentVehicle(currentVehicleId);
  }, [currentVehicleId]);

  const columns: ColumnDef<Vehicle>[] = [
    {
      accessorKey: "licensePlate",
      header: () => (
        <div
          onClick={() => {
            setSort("licensePlate");
            setOrder((prev: string) => {
              return prev == "asc" ? "desc" : "asc";
            });
          }}
          className="text-center text-base gap-1 cursor-pointer flex items-center justify-center hover:text-muted-foreground">
          License Plate
          {sort == "licensePlate" && (
            <span className="material-symbols-outlined md-18">
              {order == "asc" ? "arrow_upward" : "arrow_downward"}
            </span>
          )}
        </div>
      ),
      cell: ({ row }) => {
        const vehicle = row.original;
        return (
          <div className="flex justify-center text-sm">
            {vehicle.licensePlate}
          </div>
        );
      },
    },
    {
      accessorKey: "vin",
      header: () => (
        <div
          onClick={() => {
            setSort("vin");
            setOrder((prev: string) => {
              return prev == "asc" ? "desc" : "asc";
            });
          }}
          className="text-center text-base gap-1 cursor-pointer flex items-center justify-center hover:text-muted-foreground">
          Vin
          {sort == "vin" && (
            <span className="material-symbols-outlined md-18">
              {order == "asc" ? "arrow_upward" : "arrow_downward"}
            </span>
          )}
        </div>
      ),
      cell: ({ row }) => {
        const vehicle = row.original;
        return <div className="flex justify-center text-sm">{vehicle.vin}</div>;
      },
    },
    {
      accessorKey: "brand",
      header: () => (
        <div
          onClick={() => {
            setSort("brand");
            setOrder((prev: string) => {
              return prev == "asc" ? "desc" : "asc";
            });
          }}
          className="text-center text-base gap-1 cursor-pointer flex items-center justify-center hover:text-muted-foreground">
          Brand
          {sort == "brand" && (
            <span className="material-symbols-outlined md-18">
              {order == "asc" ? "arrow_upward" : "arrow_downward"}
            </span>
          )}
        </div>
      ),
      cell: ({ row }) => {
        const vehicle = row.original;
        return <div className="flex justify-center">{vehicle.brand}</div>;
      },
    },
    {
      accessorKey: "model",
      header: () => (
        <div
          onClick={() => {
            setSort("model");
            setOrder((prev: string) => {
              return prev == "asc" ? "desc" : "asc";
            });
          }}
          className="text-center text-base gap-1 cursor-pointer flex items-center justify-center hover:text-muted-foreground">
          Model
          {sort == "model" && (
            <span className="material-symbols-outlined md-18">
              {order == "asc" ? "arrow_upward" : "arrow_downward"}
            </span>
          )}
        </div>
      ),
      cell: ({ row }) => {
        const vehicle = row.original;
        return <div className="flex justify-center">{vehicle.model}</div>;
      },
    },
    {
      accessorKey: "year",
      header: () => (
        <div
          onClick={() => {
            setSort("year");
            setOrder((prev: string) => {
              return prev == "asc" ? "desc" : "asc";
            });
          }}
          className="text-center text-base gap-1 cursor-pointer flex items-center justify-center hover:text-muted-foreground">
          Year
          {sort == "year" && (
            <span className="material-symbols-outlined md-18">
              {order == "asc" ? "arrow_upward" : "arrow_downward"}
            </span>
          )}
        </div>
      ),
      cell: ({ row }) => {
        const vehicle = row.original;
        return <div className="flex justify-center">{vehicle.year}</div>;
      },
    },
    {
      accessorKey: "kmTravelled",
      header: () => (
        <div
          onClick={() => {
            setSort("kmTravelled");
            setOrder((prev: string) => {
              return prev == "asc" ? "desc" : "asc";
            });
          }}
          className="text-center text-base gap-1 cursor-pointer flex items-center justify-center hover:text-muted-foreground">
          Km Travelled
          {sort == "kmTravelled" && (
            <span className="material-symbols-outlined md-18">
              {order == "asc" ? "arrow_upward" : "arrow_downward"}
            </span>
          )}
        </div>
      ),
      cell: ({ row }) => {
        const vehicle = row.original;
        return <div className="flex justify-center">{vehicle.kmTravelled}</div>;
      },
    },
  ];

  return (
    <div className="m-3 flex gap-8 flex-col grow mx-8 max-w-full col-span-full">
      <h4 className="w-full scroll-m-20 text-xl font-semibold tracking-tight text-center">
        Current Vehicle
      </h4>
      <VehicleDetailsList
        vehicle={currentVehicle}
        isOutsideForm={false}></VehicleDetailsList>
      <h4 className="w-full scroll-m-20 text-xl font-semibold tracking-tight text-center">
        Select the new vehicle from these available ones
      </h4>
      <DataTable
        columns={columns}
        data={availableVehicles ? availableVehicles : []}
        enableRowSelection={true}
        enableMultiRowSelection={false}
        onRowSelection={(newVehicleId: number | undefined) => {
          onVehicleSelection(newVehicleId);
        }}
        getRowId={(row: Vehicle) => {
          return row.id.toString();
        }}
      />
      <FormField
        control={control}
        name="newVehicleId"
        render={() => (
          <FormItem className="flex w-full text-center flex-col">
            <FormMessage />
          </FormItem>
        )}
      />
      <PaginationWrapper
        currentPage={page}
        totalPages={totalPages}
        onPageChange={(value) => {
          fetchAvailableVehicles(order, sort, value);
        }}></PaginationWrapper>
    </div>
  );
}

export default ChangeVehicleOrDeleteReservationForm;
