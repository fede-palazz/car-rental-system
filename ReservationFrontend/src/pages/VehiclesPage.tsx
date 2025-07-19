import { useEffect, useRef, useState } from "react";
import { CarStatus, Vehicle } from "@/models/Vehicle";
import { ColumnDef } from "@tanstack/react-table";
import { Button } from "@/components/ui/button";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import PaginationWrapper from "@/components/ui/paginationWrapper";
import VehicleAPI from "@/API/VehiclesAPI";
import ConfirmationDialog from "@/components/ConfirmationDialog";
import { SidebarInset, SidebarTrigger } from "@/components/ui/sidebar";
import { ThemeToggler } from "@/components/ThemeToggler";
import { VehicleFilter } from "@/models/filters/VehicleFilter";
import VehicleFiltersSidebar from "@/components/Sidebars/VehicleFiltersSidebar";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { DataTable } from "@/components/ui/data-table";
import { PagedResDTO } from "@/models/dtos/response/PagedResDTO";
import { toast } from "sonner";

const VehiclesPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [vehicles, setVehicles] = useState<Vehicle[] | undefined>(undefined);
  const [deleteConfirmationOpen, setDeleteConfirmationOpen] =
    useState<boolean>(false);
  const [filtersSidebarOpen, setFiltersSidebarOpen] = useState<boolean>(false);
  const [filter, setFilter] = useState<VehicleFilter>({
    licensePlate: undefined,
    vin: undefined,
    brand: undefined,
    model: undefined,
    year: undefined,
    status: undefined,
    minKmTravelled: undefined,
    maxKmTravelled: undefined,
    pendingCleaning: undefined,
    pendingRepair: undefined,
  });
  const [order, setOrder] = useState<string>("asc");
  const [sort, setSort] = useState<string>("vin");
  const [pageSize, setPageSize] = useState<number>(9);
  const [page, setPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(1);
  const deletingOrEditingIdRef = useRef<number | undefined>(undefined);
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
      accessorKey: "status",
      header: () => (
        <div
          onClick={() => {
            setSort("status");
            setOrder((prev: string) => {
              return prev == "asc" ? "desc" : "asc";
            });
          }}
          className="text-center text-base gap-1 cursor-pointer flex items-center justify-center hover:text-muted-foreground">
          Status
          {sort == "status" && (
            <span className="material-symbols-outlined md-18">
              {order == "asc" ? "arrow_upward" : "arrow_downward"}
            </span>
          )}
        </div>
      ),
      cell: ({ row }) => {
        const vehicle = row.original;
        const formattedStatus = vehicle.status.replace(/_/g, " ");
        return (
          <div className="flex justify-center">
            {formattedStatus.charAt(0).toUpperCase() +
              formattedStatus.slice(1).toLowerCase()}
          </div>
        );
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
    {
      accessorKey: "pendingRepair",
      header: () => (
        <div
          onClick={() => {
            setSort("pendingRepair");
            setOrder((prev: string) => {
              return prev == "asc" ? "desc" : "asc";
            });
          }}
          className="text-center text-base gap-1 cursor-pointer flex items-center justify-center hover:text-muted-foreground">
          Pending Repair
          {sort == "pendingRepair" && (
            <span className="material-symbols-outlined md-18">
              {order == "asc" ? "arrow_upward" : "arrow_downward"}
            </span>
          )}
        </div>
      ),
      cell: ({ row }) => {
        const vehicle = row.original;
        return (
          <div className="flex justify-center">
            {vehicle.pendingRepair ? "Yes" : "No"}
          </div>
        );
      },
    },
    {
      accessorKey: "pendingCleaning",
      header: () => (
        <div
          onClick={() => {
            setSort("pendingCleaning");
            setOrder((prev: string) => {
              return prev == "asc" ? "desc" : "asc";
            });
          }}
          className="text-center text-base gap-1 cursor-pointer flex items-center justify-center hover:text-muted-foreground">
          Pending Cleaning
          {sort == "pendingCleaning" && (
            <span className="material-symbols-outlined md-18">
              {order == "asc" ? "arrow_upward" : "arrow_downward"}
            </span>
          )}
        </div>
      ),
      cell: ({ row }) => {
        const vehicle = row.original;
        return (
          <div className="flex justify-center">
            {vehicle.pendingCleaning ? "Yes" : "No"}
          </div>
        );
      },
    },
    {
      id: "actions",
      header: () => (
        <div className=" text-center flex items-center justify-center">
          Actions
        </div>
      ),
      cell: ({ row }) => {
        const vehicle = row.original;
        return (
          <div className="flex gap-3 justify-center">
            <Button
              variant="ghost"
              size="icon"
              onClick={(e) => {
                e.stopPropagation();
                navigate(`${vehicle.id}`);
              }}>
              <span className="material-symbols-outlined md-18">info</span>
            </Button>
            <Button
              variant="destructive"
              size="icon"
              onClick={(e) => {
                e.stopPropagation();
                deletingOrEditingIdRef.current = vehicle.id;
                setDeleteConfirmationOpen(true);
              }}>
              <span className="material-symbols-outlined md-18">delete</span>
            </Button>
            <Button
              variant="secondary"
              size="icon"
              onClick={(e) => {
                e.stopPropagation();
                deletingOrEditingIdRef.current = vehicle.id;
                navigate("edit");
              }}>
              <span className="material-symbols-outlined md-18">edit</span>
            </Button>
          </div>
        );
      },
    },
  ];

  const fetchVehicles = (
    filter: VehicleFilter,
    orderParam: string = order,
    sortParam: string = sort,
    pageParam: number = page,
    pageSizeParam: number = pageSize
  ) => {
    VehicleAPI.getAllVehicles(
      filter,
      orderParam,
      sortParam,
      pageParam,
      pageSizeParam
    )
      .then((vehicles: PagedResDTO<Vehicle>) => {
        setVehicles(vehicles.content);
        setPage(vehicles.currentPage);
        setTotalPages(vehicles.totalPages);
        //setPageSize(vehicles.elementsInPage);
      })
      .catch((err) => {
        console.log(err);
      });
  };

  const handleDelete = () => {
    VehicleAPI.deleteVehicleById(Number(deletingOrEditingIdRef.current))
      .then(() => {
        setDeleteConfirmationOpen(false);
        deletingOrEditingIdRef.current = undefined;
        fetchVehicles(filter);
      })
      .catch((err) => {
        console.log(err);
      });
  };

  useEffect(() => {
    if (location.pathname !== "/vehicles") return;
    fetchVehicles(filter, order, sort);
  }, [location.pathname, order, sort]);

  return (
    <>
      <SidebarInset
        id="sidebar-inset"
        className="p-2 flex flex-col w-full  overflow-x-auto ">
        <div className=" flex items-center justify-between border-b">
          <SidebarTrigger />
          {
            <h1 className=" p-2 pb-3 text-3xl font-bold tracking-tight first:mt-0">{`Vehicles`}</h1>
          }
          <ThemeToggler></ThemeToggler>
        </div>
        <div className="grow flex flex-col">
          <div className="flex mt-3 justify-between mx-8">
            <div className="flex">
              <Tabs
                defaultValue="undefined"
                value={filter.status || "undefined"}
                onValueChange={(value) => {
                  const newFilter = {
                    ...filter,
                    status:
                      value !== "undefined" ? (value as CarStatus) : undefined,
                  };
                  fetchVehicles(newFilter, order, sort, 0);
                  setFilter(newFilter);
                }}
                className="w-[400px]">
                <TabsList>
                  <TabsTrigger value="undefined">All</TabsTrigger>
                  <TabsTrigger value="AVAILABLE">Available</TabsTrigger>
                  <TabsTrigger value="RENTED">Rented</TabsTrigger>
                  <TabsTrigger value="IN_MAINTENANCE">
                    In Maintenance
                  </TabsTrigger>
                </TabsList>
              </Tabs>
            </div>
            <div className="flex gap-3 items-center">
              <Button
                variant="secondary"
                onClick={(e) => {
                  e.stopPropagation();
                  setFiltersSidebarOpen((prev) => !prev);
                }}>
                <span className="material-symbols-outlined md-18">
                  filter_alt
                </span>
                Filters
              </Button>
              <Button
                variant="default"
                size="lg"
                onClick={(e) => {
                  e.stopPropagation();
                  deletingOrEditingIdRef.current = undefined;
                  navigate("add");
                }}>
                <span className="material-symbols-outlined md-18">add</span>
                Create vehicle
              </Button>
            </div>
          </div>
          <div className="m-3 flex gap-2 flex-col grow mx-8 max-w-full">
            <DataTable
              columns={columns}
              data={vehicles ? vehicles : []}></DataTable>
            <div className="mt-auto flex">
              <PaginationWrapper
                currentPage={page}
                totalPages={totalPages}
                onPageChange={(value) => {
                  fetchVehicles(filter, order, sort, value);
                }}></PaginationWrapper>
            </div>
          </div>
        </div>
        <ConfirmationDialog
          open={deleteConfirmationOpen}
          handleSubmit={handleDelete}
          title="Delete Confirmation"
          submitButtonLabel="Delete"
          submitButtonVariant="destructive"
          content="Are you sure to delete this vehicle?"
          description="This action is irreversible"
          descriptionClassName="text-warning"
          handleCancel={() => {
            deletingOrEditingIdRef.current = undefined;
            setDeleteConfirmationOpen(false);
          }}></ConfirmationDialog>
        <Outlet
          context={
            deletingOrEditingIdRef.current
              ? vehicles?.find(
                  (vehicle) => vehicle.id === deletingOrEditingIdRef.current
                )
              : undefined
          }></Outlet>
      </SidebarInset>
      {filtersSidebarOpen && (
        <VehicleFiltersSidebar
          mobileOpen={filtersSidebarOpen}
          setMobileOpen={setFiltersSidebarOpen}
          filter={filter}
          fetchVehicles={(filter, order, sort) =>
            fetchVehicles(filter, order, sort, 0)
          }
          setFilter={setFilter}></VehicleFiltersSidebar>
      )}
    </>
  );
};

export default VehiclesPage;
