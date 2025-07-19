import { useContext, useEffect, useRef, useState } from "react";
import { ColumnDef } from "@tanstack/react-table";
import { Button } from "@/components/ui/button";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import PaginationWrapper from "@/components/ui/paginationWrapper";
import ConfirmationDialog from "@/components/ConfirmationDialog";
import { SidebarInset, SidebarTrigger } from "@/components/ui/sidebar";
import { ThemeToggler } from "@/components/ThemeToggler";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { DataTable } from "@/components/ui/data-table";
import { PagedResDTO } from "@/models/dtos/response/PagedResDTO";
import UserContext from "@/contexts/UserContext";
import { Reservation } from "@/models/Reservation";
import { ReservationFilter } from "@/models/filters/ReservationFilter";
import ReservationsAPI from "@/API/ReservationsAPI";
import { UserRole } from "@/models/enums/UserRole";
import { format } from "date-fns";
import { Separator } from "@/components/ui/separator";
import { ReservationStatus } from "@/models/enums/ReservationStatus";
import ReservationFiltersSidebar from "@/components/Sidebars/ReservationFilterSidebar";
import PendingReservation from "@/components/PendingReservation";
import { toast } from "sonner";
import UserAPI from "@/API/UserAPI";
import { User } from "@/models/User";

function ReservationsPage() {
  const navigate = useNavigate();
  const user = useContext(UserContext);
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const paymentOutcome: boolean | null =
    searchParams.get("completed") === null
      ? null
      : !!searchParams.get("completed");
  const [reservations, setReservations] = useState<Reservation[] | undefined>(
    undefined
  );
  const [pendingReservation, setPendingReservation] = useState<
    Reservation | undefined
  >(undefined);
  const [deleteConfirmationOpen, setDeleteConfirmationOpen] =
    useState<boolean>(false);
  const [filtersSidebarOpen, setFiltersSidebarOpen] = useState<boolean>(false);
  const [filter, setFilter] = useState<ReservationFilter>({
    licensePlate: undefined,
    vin: undefined,
    brand: undefined,
    model: undefined,
    year: undefined,
    minCreationDate: undefined,
    maxCreationDate: undefined,
    minPlannedPickUpDate: undefined,
    maxPlannedPickUpDate: undefined,
    minActualPickUpDate: undefined,
    maxActualPickUpDate: undefined,
    minPlannedDropOffDate: undefined,
    maxPlannedDropOffDate: undefined,
    minActualDropOffDate: undefined,
    maxActualDropOffDate: undefined,
    status: undefined,
    wasDeliveryLate: undefined,
    wasChargedFee: undefined,
    wasVehicleDamaged: undefined,
    wasInvolvedInAccident: undefined,
  });
  const [order, setOrder] = useState<string>("desc");
  const [sort, setSort] = useState<string>("plannedPickUpDate");
  const [pageSize, setPageSize] = useState<number>(9);
  const [page, setPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(1);
  const deletingOrEditingIdRef = useRef<number | undefined>(undefined);

  const fetchReservations = (
    filter: ReservationFilter,
    orderParam: string = order,
    sortParam: string = sort,
    pageParam: number = page,
    pageSizeParam: number = pageSize
  ) => {
    ReservationsAPI.getAllReservations(
      filter,
      orderParam,
      sortParam,
      pageParam,
      pageSizeParam,
      user && user.role == UserRole.CUSTOMER
    )
      .then((res: PagedResDTO<Reservation>) => {
        if (user != undefined && user.role == UserRole.CUSTOMER) {
          const pending = res.content.find(
            (r) => r.status === ReservationStatus.PENDING
          );
          setPendingReservation(pending);
          setReservations(
            res.content.filter((r) => r.status !== ReservationStatus.PENDING)
          );
        } else {
          setReservations(res.content);
        }
        setPage(res.currentPage);
        setTotalPages(res.totalPages);
        //setPageSize(vehicles.elementsInPage);
      })
      .catch((err) => {
        //console.log(err);
        console.log(err);
      });
  };

  const handleDelete = () => {
    ReservationsAPI.deleteReservationById(
      Number(deletingOrEditingIdRef.current)
    )
      .then(() => {
        setDeleteConfirmationOpen(false);
        deletingOrEditingIdRef.current = undefined;
        fetchReservations(filter);
      })
      .catch((err) => {
        toast.error("Error");
      });
  };

  useEffect(() => {
    if (location.pathname !== "/reservations") return;

    fetchReservations(filter, order, sort);
    if (paymentOutcome == null) return;
    if (paymentOutcome) {
      toast.success("Payment succeeded");
    } else {
      toast.error("Payment failed");
    }
  }, [location, paymentOutcome, order, sort, user]);

  useEffect(() => {
    if (paymentOutcome == null) return;
    if (pendingReservation == undefined) {
      // No pending reservations, no polling needed
      return;
    }
    const interval = setInterval(() => {
      fetchReservations(filter, order, sort);
    }, 1000);

    return () => clearInterval(interval);
  }, [paymentOutcome, pendingReservation]);

  const baseColumns: ColumnDef<Reservation>[] = [
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
        const reservation = row.original;
        return (
          <div className="flex justify-center text-sm">
            {reservation.licensePlate}
          </div>
        );
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
        const reservation = row.original;
        const formattedStatus = reservation.status.replace(/_/g, " ");
        return (
          <div className="flex justify-center">
            {formattedStatus.charAt(0).toUpperCase() +
              formattedStatus.slice(1).toLowerCase()}
          </div>
        );
      },
    },
    {
      accessorKey: "creationDate",
      header: () => (
        <div
          onClick={() => {
            setSort("creationDate");
            setOrder((prev: string) => {
              return prev == "asc" ? "desc" : "asc";
            });
          }}
          className="text-center text-base gap-1 cursor-pointer flex items-center justify-center hover:text-muted-foreground">
          <div className="flex flex-col text-center text-base leading-tight">
            <span>Creation</span>
            <span>Date</span>
          </div>
          {sort == "creationDate" && (
            <span className="material-symbols-outlined md-18">
              {order == "asc" ? "arrow_upward" : "arrow_downward"}
            </span>
          )}
        </div>
      ),
      cell: ({ row }) => {
        const reservation = row.original;
        return (
          <div className="flex justify-center text-sm">
            {format(reservation.creationDate, "dd/MM/yyy")}
          </div>
        );
      },
    },
    {
      accessorKey: "plannedPickUpDate",
      header: () => (
        <div>
          <div className="flex flex-col py-1 gap-1">
            <div className="flex text-center text-base justify-center">
              Pickup Date
            </div>
            <div className="flex gap-1">
              <div
                onClick={() => {
                  setSort("plannedPickUpDate");
                  setOrder((prev: string) => {
                    return prev == "asc" ? "desc" : "asc";
                  });
                }}
                className="text-center text-sm gap-1 cursor-pointer flex items-center justify-center hover:text-muted-foreground w-1/2">
                Planned
                {sort == "plannedPickUpDate" && (
                  <span className="material-symbols-outlined md-18">
                    {order == "asc" ? "arrow_upward" : "arrow_downward"}
                  </span>
                )}
              </div>
              <div>
                <Separator orientation="vertical" />
              </div>
              <div
                onClick={() => {
                  setSort("actualPickUpDate");
                  setOrder((prev: string) => {
                    return prev == "asc" ? "desc" : "asc";
                  });
                }}
                className="text-center text-sm gap-1 cursor-pointer flex items-center justify-center hover:text-muted-foreground w-1/2">
                Actual
                {sort == "actualPickUpDate" && (
                  <span className="material-symbols-outlined md-18">
                    {order == "asc" ? "arrow_upward" : "arrow_downward"}
                  </span>
                )}
              </div>
            </div>
          </div>
        </div>
      ),
      cell: ({ row }) => {
        const reservation = row.original;
        return (
          <div className="flex gap-1">
            <div className="flex flex-col items-center justify-center text-sm w-1/2">
              <p>{format(reservation.plannedPickUpDate, "dd/MM/yyy")}</p>
              <p>{format(reservation.plannedPickUpDate, "HH:mm")}</p>
            </div>
            <div className="flex justify-center text-sm">
              <Separator orientation="vertical" />
            </div>
            <div className="flex flex-col items-center justify-center text-sm w-1/2">
              {reservation.actualPickUpDate ? (
                <>
                  <p>{format(reservation.actualPickUpDate, "dd/MM/yyy")}</p>
                  <p>{format(reservation.actualPickUpDate, "HH:mm")}</p>
                </>
              ) : (
                "-"
              )}
            </div>
          </div>
        );
      },
    },
    {
      accessorKey: "plannedDropOffDate",
      header: () => (
        <div>
          <div className="flex flex-col py-1 gap-1">
            <div className="flex text-center text-base justify-center">
              Dropoff Date
            </div>
            <div className="flex gap-1">
              <div
                onClick={() => {
                  setSort("plannedDropOffDate");
                  setOrder((prev: string) => {
                    return prev == "asc" ? "desc" : "asc";
                  });
                }}
                className="text-center text-sm gap-1 cursor-pointer flex items-center justify-center hover:text-muted-foreground w-1/2">
                Planned
                {sort == "plannedDropOffDate" && (
                  <span className="material-symbols-outlined md-18">
                    {order == "asc" ? "arrow_upward" : "arrow_downward"}
                  </span>
                )}
              </div>
              <div>
                <Separator orientation="vertical" />
              </div>
              <div
                onClick={() => {
                  setSort("actualDropOffDate");
                  setOrder((prev: string) => {
                    return prev == "asc" ? "desc" : "asc";
                  });
                }}
                className="text-center text-sm gap-1 cursor-pointer flex items-center justify-center hover:text-muted-foreground w-1/2">
                Actual
                {sort == "actualDropOffDate" && (
                  <span className="material-symbols-outlined md-18">
                    {order == "asc" ? "arrow_upward" : "arrow_downward"}
                  </span>
                )}
              </div>
            </div>
          </div>
        </div>
      ),
      cell: ({ row }) => {
        const reservation = row.original;
        return (
          <div className="flex gap-1">
            <div className="flex flex-col items-center justify-center text-sm w-1/2">
              <p>{format(reservation.plannedDropOffDate, "dd/MM/yyy")}</p>
              <p>{format(reservation.plannedDropOffDate, "HH:mm")}</p>
            </div>
            <div className="flex justify-center text-sm">
              <Separator orientation="vertical" />
            </div>
            <div className="flex flex-col items-center justify-center text-sm w-1/2">
              {reservation.actualDropOffDate ? (
                <>
                  <p>{format(reservation.actualDropOffDate, "dd/MM/yyy")}</p>
                  <p>{format(reservation.actualDropOffDate, "HH:mm")}</p>
                </>
              ) : (
                "-"
              )}
            </div>
          </div>
        );
      },
    },
  ];

  const staffColumns: ColumnDef<Reservation>[] = [
    {
      accessorKey: "customerUsername",
      header: () => (
        <div
          onClick={() => {
            setSort("customerUsername");
            setOrder((prev: string) => {
              return prev == "asc" ? "desc" : "asc";
            });
          }}
          className="text-center text-base gap-1 cursor-pointer flex items-center justify-center hover:text-muted-foreground">
          Customer
          {sort == "customerUsername" && (
            <span className="material-symbols-outlined md-18">
              {order == "asc" ? "arrow_upward" : "arrow_downward"}
            </span>
          )}
        </div>
      ),
      cell: ({ row }) => {
        const reservation = row.original;
        return (
          <div className="flex justify-center text-sm">
            {reservation.customerUsername}
          </div>
        );
      },
    },
    {
      accessorKey: "wasDeliveryLate",
      header: () => (
        <div
          onClick={() => {
            setSort("wasDeliveryLate");
            setOrder((prev: string) => {
              return prev == "asc" ? "desc" : "asc";
            });
          }}
          className="text-center text-base gap-1 cursor-pointer flex items-center justify-center hover:text-muted-foreground">
          <div className="flex flex-col text-center text-base leading-tight">
            <span>Late</span>
            <span>Delivery</span>
          </div>
          {sort == "wasDeliveryLate" && (
            <span className="material-symbols-outlined md-18">
              {order == "asc" ? "arrow_upward" : "arrow_downward"}
            </span>
          )}
        </div>
      ),
      cell: ({ row }) => {
        const reservation = row.original;
        return (
          <div className="flex justify-center">
            {reservation.wasDeliveryLate == null
              ? "-"
              : reservation.wasDeliveryLate
              ? "Yes"
              : "No"}
          </div>
        );
      },
    },
    {
      accessorKey: "wasChargedFee",
      header: () => (
        <div
          onClick={() => {
            setSort("wasChargedFee");
            setOrder((prev: string) => {
              return prev == "asc" ? "desc" : "asc";
            });
          }}
          className="text-center text-base gap-1 cursor-pointer flex items-center justify-center hover:text-muted-foreground">
          <div className="flex flex-col text-center text-base leading-tight">
            <span>Charged</span>
            <span>Fee</span>
          </div>
          {sort == "wasChargedFee" && (
            <span className="material-symbols-outlined md-18">
              {order == "asc" ? "arrow_upward" : "arrow_downward"}
            </span>
          )}
        </div>
      ),
      cell: ({ row }) => {
        const reservation = row.original;
        return (
          <div className="flex justify-center">
            {reservation.wasChargedFee == null
              ? "-"
              : reservation.wasChargedFee
              ? "Yes"
              : "No"}
          </div>
        );
      },
    },
    {
      accessorKey: "wasVehicleDamaged",
      header: () => (
        <div
          onClick={() => {
            setSort("wasVehicleDamaged");
            setOrder((prev: string) => {
              return prev == "asc" ? "desc" : "asc";
            });
          }}
          className="text-center text-base gap-1 cursor-pointer flex items-center justify-center hover:text-muted-foreground">
          <div className="flex flex-col text-center text-base leading-tight">
            <span>Damaged</span>
            <span>Vehicle</span>
          </div>
          {sort == "wasVehicleDamaged" && (
            <span className="material-symbols-outlined md-18">
              {order == "asc" ? "arrow_upward" : "arrow_downward"}
            </span>
          )}
        </div>
      ),
      cell: ({ row }) => {
        const reservation = row.original;
        return (
          <div className="flex justify-center">
            {reservation.wasVehicleDamaged == null
              ? "-"
              : reservation.wasVehicleDamaged
              ? "Yes"
              : "No"}
          </div>
        );
      },
    },
    {
      accessorKey: "wasInvolvedInAccident",
      header: () => (
        <div
          onClick={() => {
            setSort("wasInvolvedInAccident");
            setOrder((prev: string) => {
              return prev == "asc" ? "desc" : "asc";
            });
          }}
          className="text-center text-base gap-1 cursor-pointer flex items-center justify-center hover:text-muted-foreground">
          Accident
          {sort == "wasInvolvedInAccident" && (
            <span className="material-symbols-outlined md-18">
              {order == "asc" ? "arrow_upward" : "arrow_downward"}
            </span>
          )}
        </div>
      ),
      cell: ({ row }) => {
        const reservation = row.original;
        return (
          <div className="flex justify-center">
            {reservation.wasInvolvedInAccident == null
              ? "-"
              : reservation.wasInvolvedInAccident
              ? "Yes"
              : "No"}
          </div>
        );
      },
    },
  ];

  const columns: ColumnDef<Reservation>[] = [
    ...(user && user.role !== UserRole.CUSTOMER ? [staffColumns[0]] : []),
    ...baseColumns,
    ...(user && user.role !== UserRole.CUSTOMER ? staffColumns.slice(1) : []),
    {
      id: "actions",
      header: () => (
        <div className=" text-center flex items-center justify-center">
          Actions
        </div>
      ),
      cell: ({ row }) => {
        const reservation = row.original;
        return (
          <div className="flex gap-1 justify-center">
            <Button
              variant="destructive"
              title="Delete"
              size="icon"
              disabled={
                reservation.actualPickUpDate != undefined ||
                (reservation.plannedPickUpDate &&
                  reservation.plannedPickUpDate < new Date())
              }
              onClick={(e) => {
                e.stopPropagation();
                deletingOrEditingIdRef.current = reservation.id;
                setDeleteConfirmationOpen(true);
              }}>
              <span className="material-symbols-outlined md-18">delete</span>
            </Button>
            {/*<Button
              variant="secondary"
              title="Edit"
              size="icon"
              disabled={reservation.plannedPickUpDate <= new Date()}
              onClick={(e) => {
                e.stopPropagation();
                deletingOrEditingIdRef.current = reservation.id;
                navigate("edit");
            }}>
              <span className="material-symbols-outlined md-18">edit</span>
            </Button>*/}
            {user && user.role != UserRole.CUSTOMER && (
              <>
                <Button
                  variant="secondary"
                  title="Start"
                  size="icon"
                  disabled={!!reservation.actualPickUpDate}
                  onClick={(e) => {
                    e.stopPropagation();
                    deletingOrEditingIdRef.current = reservation.id;
                    navigate("pick-up-date");
                  }}>
                  <span className="material-symbols-outlined md-18">
                    event_available
                  </span>
                </Button>
                <Button
                  title="Finalize"
                  size="icon"
                  disabled={
                    !!reservation.actualDropOffDate ||
                    !reservation.actualPickUpDate
                  }
                  onClick={(e) => {
                    e.stopPropagation();
                    deletingOrEditingIdRef.current = reservation.id;
                    navigate("finalize");
                  }}>
                  <span className="material-symbols-outlined md-18">
                    handshake
                  </span>
                </Button>
              </>
            )}
          </div>
        );
      },
    },
  ];

  return (
    <>
      <SidebarInset
        id="sidebar-inset"
        className="p-2 flex flex-col w-full  overflow-x-auto ">
        <div className=" flex items-center justify-between border-b">
          <SidebarTrigger />
          {
            <h1 className=" p-2 pb-3 text-3xl font-bold tracking-tight first:mt-0">{`Reservations`}</h1>
          }
          <ThemeToggler></ThemeToggler>
        </div>
        <div className="grow flex flex-col">
          {pendingReservation && (
            <>
              <PendingReservation
                reservation={pendingReservation}
                handleCancel={(e) => {
                  e.stopPropagation();
                  deletingOrEditingIdRef.current = pendingReservation.id;
                  setDeleteConfirmationOpen(true);
                }}></PendingReservation>
              <Separator></Separator>
            </>
          )}
          <div className="flex mt-3 justify-between mx-8">
            <div className="flex">
              <Tabs
                defaultValue="undefined"
                value={filter.status || "undefined"}
                onValueChange={(value) => {
                  const newFilter = {
                    ...filter,
                    status:
                      value !== "undefined"
                        ? (value as ReservationStatus)
                        : undefined,
                  };
                  fetchReservations(newFilter, order, sort, 0);
                  setFilter(newFilter);
                }}
                className="w-[400px]">
                <TabsList>
                  <TabsTrigger value="undefined">All</TabsTrigger>
                  <TabsTrigger value="CONFIRMED">Confirmed</TabsTrigger>
                  <TabsTrigger value="DELIVERED">Delivered</TabsTrigger>
                  <TabsTrigger value="PICKED_UP">Picked Up</TabsTrigger>
                  {user && user.role !== UserRole.CUSTOMER && (
                    <TabsTrigger value="PENDING">Pending</TabsTrigger>
                  )}
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
            </div>
          </div>
          <div className="m-3 flex gap-2 flex-col grow mx-8 max-w-full">
            <DataTable
              columns={columns}
              data={
                reservations
                  ? user && user.role === UserRole.CUSTOMER
                    ? reservations.filter(
                        (r) => r.status !== ReservationStatus.PENDING
                      )
                    : reservations
                  : []
              }></DataTable>
            <div className="mt-auto flex">
              <PaginationWrapper
                currentPage={page}
                totalPages={totalPages}
                onPageChange={(value) => {
                  fetchReservations(filter, order, sort, value);
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
          content="Are you sure to delete this reservation?"
          description="This action is irreversible"
          descriptionClassName="text-warning"
          handleCancel={() => {
            deletingOrEditingIdRef.current = undefined;
            setDeleteConfirmationOpen(false);
          }}></ConfirmationDialog>
        <Outlet
          context={
            deletingOrEditingIdRef.current
              ? reservations?.find(
                  (reservation) =>
                    reservation.id === deletingOrEditingIdRef.current
                )
              : undefined
          }></Outlet>
      </SidebarInset>
      {filtersSidebarOpen && (
        <ReservationFiltersSidebar
          mobileOpen={filtersSidebarOpen}
          setMobileOpen={setFiltersSidebarOpen}
          filter={filter}
          fetchReservations={(filter, order, sort) =>
            fetchReservations(filter, order, sort, 0)
          }
          setFilter={setFilter}></ReservationFiltersSidebar>
      )}
    </>
  );
}

export default ReservationsPage;
