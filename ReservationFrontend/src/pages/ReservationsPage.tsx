import { useContext, useEffect, useState } from "react";
import { ColumnDef } from "@tanstack/react-table";
import { Button } from "@/components/ui/button";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import PaginationWrapper from "@/components/ui/paginationWrapper";
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
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Badge } from "@/components/ui/badge";
import { damageAndDirtinessLevelLabels } from "@/utils/damageAndDirtinessLevelLabels";

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
    wasInvolvedInAccident: undefined,
  });
  const [order, setOrder] = useState<string>("desc");
  const [sort, setSort] = useState<string>("plannedPickUpDate");
  const [pageSize, setPageSize] = useState<number>(9);
  const [page, setPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(1);

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
      .catch((err: Error) => {
        toast.error(err.message);
      });
    //Fetch the pending reservation if it is a customer
    if (user != undefined && user.role == UserRole.CUSTOMER) {
      ReservationsAPI.getPendingReservation()
        .then((res: PagedResDTO<Reservation>) => {
          const pending = res.content[0];
          setPendingReservation(pending);
        })
        .catch((err: Error) => {
          toast.error(err.message);
        });
    }
  };

  useEffect(() => {
    fetchReservations(filter, order, sort);

    if (paymentOutcome != null) {
      if (paymentOutcome) toast.success("Payment succeeded");
      else toast.error("Payment failed");
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
        const reservation = row.original;
        return (
          <div className="flex justify-center text-sm">
            {`${reservation.brand} ${reservation.model} ${reservation.year} `}
          </div>
        );
      },
    },
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
      accessorKey: "totalAmount",
      header: () => (
        <div
          onClick={() => {
            setSort("totalAmount");
            setOrder((prev: string) => {
              return prev == "asc" ? "desc" : "asc";
            });
          }}
          className="text-center text-base gap-1 cursor-pointer flex items-center justify-center hover:text-muted-foreground">
          Amount
          {sort == "totalAmount" && (
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
            {reservation.totalAmount.toFixed(2)} €
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
    {
      accessorKey: "damageLevel",
      header: () => (
        <div
          onClick={() => {
            setSort("damageLevel");
            setOrder((prev: string) => {
              return prev == "asc" ? "desc" : "asc";
            });
          }}
          className="text-center text-base gap-1 cursor-pointer flex items-center justify-center hover:text-muted-foreground">
          <div className="flex flex-col text-center text-base leading-tight">
            <span>Damage</span>
            <span>Level</span>
          </div>
          {sort == "damageLevel" && (
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
            {reservation.damageLevel == null ? (
              "-"
            ) : (
              <Badge variant={"default"}>
                {damageAndDirtinessLevelLabels[reservation.damageLevel]}
              </Badge>
            )}
          </div>
        );
      },
    },
    {
      accessorKey: "dirtinessLevel",
      header: () => (
        <div
          onClick={() => {
            setSort("dirtinessLevel");
            setOrder((prev: string) => {
              return prev == "asc" ? "desc" : "asc";
            });
          }}
          className="text-center text-base gap-1 cursor-pointer flex items-center justify-center hover:text-muted-foreground">
          <div className="flex flex-col text-center text-base leading-tight">
            <span>Dirtiness</span>
            <span>Level</span>
          </div>
          {sort == "dirtinessLevel" && (
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
            {reservation.dirtinessLevel == null ? (
              "-"
            ) : (
              <Badge variant={"default"}>
                {damageAndDirtinessLevelLabels[reservation.dirtinessLevel]}
              </Badge>
            )}
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

        const cancelledOrExpired =
          reservation.status == ReservationStatus.CANCELLED ||
          reservation.status == ReservationStatus.EXPIRED;

        return (
          <div className="flex justify-center">
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" className="h-8 w-8 p-0 ">
                  <span className="sr-only">Open menu</span>
                  <span className="material-symbols-outlined md-18">
                    more_horiz
                  </span>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent
                className="min-w-56 rounded-lg p-2"
                side={"bottom"}
                align="end"
                sideOffset={4}>
                <DropdownMenuLabel className="font-extrabold text-center">
                  Actions
                </DropdownMenuLabel>
                <DropdownMenuSeparator />
                {user?.role !== UserRole.CUSTOMER && (
                  <DropdownMenuGroup>
                    <DropdownMenuItem
                      className=" flex items-center px-2 py-1.5 text-sm outline-hidden select-none gap-2 font-normal"
                      disabled={
                        cancelledOrExpired ||
                        reservation.actualPickUpDate != undefined ||
                        (reservation.plannedPickUpDate &&
                          reservation.plannedPickUpDate < new Date())
                      }
                      onClick={(e) => {
                        e.stopPropagation();
                        navigate(`change-vehicle/${reservation.id}`);
                      }}>
                      <span className="material-symbols-outlined md-18">
                        edit_road
                      </span>
                      Change Vehicle
                    </DropdownMenuItem>
                    <DropdownMenuItem
                      className=" flex items-center px-2 py-1.5 text-sm outline-hidden select-none gap-2 font-normal"
                      disabled={
                        cancelledOrExpired ||
                        reservation.actualPickUpDate != undefined
                      }
                      onClick={(e) => {
                        e.stopPropagation();
                        navigate(`pick-up-date/${reservation.id}`);
                      }}>
                      <span className="material-symbols-outlined md-18">
                        event_available
                      </span>
                      Start
                    </DropdownMenuItem>
                    <DropdownMenuItem
                      className=" flex items-center px-2 py-1.5 text-sm outline-hidden select-none gap-2 font-normal"
                      disabled={
                        (cancelledOrExpired ||
                          !!reservation.actualDropOffDate ||
                          !reservation.actualPickUpDate) &&
                        false
                      }
                      onClick={(e) => {
                        e.stopPropagation();
                        navigate(`finalize/${reservation.id}`);
                      }}>
                      <span className="material-symbols-outlined md-18">
                        handshake
                      </span>
                      Finalize
                    </DropdownMenuItem>
                  </DropdownMenuGroup>
                )}
                <DropdownMenuGroup>
                  <DropdownMenuItem
                    variant="destructive"
                    className=" flex items-center px-2 py-1.5 text-sm outline-hidden select-none gap-2 font-normal"
                    disabled={
                      reservation.actualPickUpDate != undefined ||
                      (reservation.plannedPickUpDate &&
                        reservation.plannedPickUpDate < new Date())
                    }
                    onClick={(e) => {
                      e.stopPropagation();
                      navigate(`delete/${reservation.id}`);
                    }}>
                    <span className="material-symbols-outlined md-18">
                      delete
                    </span>
                    Cancel
                  </DropdownMenuItem>
                </DropdownMenuGroup>
              </DropdownMenuContent>
            </DropdownMenu>
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
                reservation={pendingReservation}></PendingReservation>
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
                className="w-[600px]">
                <TabsList>
                  <TabsTrigger value="undefined">All</TabsTrigger>
                  <TabsTrigger value="CONFIRMED">Confirmed</TabsTrigger>
                  <TabsTrigger value="DELIVERED">Delivered</TabsTrigger>
                  <TabsTrigger value="PICKED_UP">Picked Up</TabsTrigger>
                  <TabsTrigger value="EXPIRED">Expired</TabsTrigger>
                  <TabsTrigger value="CANCELLED">Cancelled</TabsTrigger>
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
        <Outlet></Outlet>
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
