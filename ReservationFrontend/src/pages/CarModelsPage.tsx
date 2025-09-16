import {
    Card,
    CardContent,
    CardDescription,
    CardFooter,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import {CarModel} from "@/models/CarModel.ts";
import {useContext, useEffect, useRef, useState} from "react";
import DefaultCar from "../assets/defaultCarModel.png";
import {Button} from "@/components/ui/button";
import {Outlet, useLocation, useNavigate} from "react-router-dom";
import ConfirmationDialog from "@/components/ConfirmationDialog";
import CarModelAPI from "@/API/CarModelsAPI";
import ModelFiltersSidebar from "@/components/Sidebars/ModelFiltersSidebar";
import {ThemeToggler} from "@/components/ThemeToggler";
import {SidebarInset, SidebarTrigger} from "@/components/ui/sidebar";
import {Tabs, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {CarModelFilter} from "@/models/filters/CarModelFilter";
import {EngineType} from "@/models/enums/EngineType";
import {
    Select,
    SelectContent,
    SelectGroup,
    SelectItem,
    SelectSeparator,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import {RadioGroup, RadioGroupItem} from "@/components/ui/radio-group";
import {Label} from "@/components/ui/label";
import {PagedResDTO} from "@/models/dtos/response/PagedResDTO";
import PaginationWrapper from "@/components/ui/paginationWrapper";
import {DateRange} from "react-day-picker";
import UserContext from "@/contexts/UserContext";
import {UserRole} from "@/models/enums/UserRole";

import {DateTimePicker} from "@/components/ui/date-time-picker";
import {Input} from "@/components/ui/input";

function CarModelsPage({
                           date,
                           setDate,
                       }: {
    date: DateRange | undefined;
    setDate: (value: DateRange | undefined) => void;
}) {
    const user = useContext(UserContext);
    const navigate = useNavigate();
    const location = useLocation();
    const [carModels, setCarModels] = useState<CarModel[] | undefined>(undefined);
    const [deleteConfirmationOpen, setDeleteConfirmationOpen] =
        useState<boolean>(false);
    const [filtersSidebarOpen, setFiltersSidebarOpen] = useState<boolean>(false);
    const [filter, setFilter] = useState<CarModelFilter>({
        brand: undefined,
        model: undefined,
        year: undefined,
        search: undefined,
        segment: undefined,
        category: undefined,
        engineType: undefined,
        transmissionType: undefined,
        drivetrain: undefined,
        minRentalPrice: undefined,
        maxRentalPrice: undefined,
    });
    const [order, setOrder] = useState<string>("asc");
    const [sort, setSort] = useState<string>("brand");
    const [pageSize, setPageSize] = useState<number>(9);
    const [page, setPage] = useState<number>(0);
    const [totalPages, setTotalPages] = useState<number>(1);
    const deletingOrEditingIdRef = useRef<number | undefined>(undefined);

    const fetchModels = (
        filter: CarModelFilter,
        orderParam: string = order,
        sortParam: string = sort,
        pageParam: number = page,
        pageSizeParam: number = pageSize,
        dateRange: DateRange | undefined = date
    ) => {
        if (dateRange) {
            CarModelAPI.getAvailableModels(
                dateRange,
                filter,
                orderParam,
                sortParam,
                pageParam,
                pageSizeParam
            )
                .then((models: PagedResDTO<CarModel>) => {
                    setCarModels(models.content);
                    setPage(models.currentPage);
                    setTotalPages(models.totalPages);
                    //setPageSize(models.elementsInPage);
                })
                .catch((err) => {
                    console.log(err);
                });
        } else {
            CarModelAPI.getAllModels(
                filter,
                orderParam,
                sortParam,
                pageParam,
                pageSizeParam
            )
                .then((models: PagedResDTO<CarModel>) => {
                    setCarModels(models.content);
                    setPage(models.currentPage);
                    setTotalPages(models.totalPages);
                    //setPageSize(models.elementsInPage);
                })
                .catch((err) => {
                    console.log(err);
                });
        }
    };

    const handleDelete = () => {
        CarModelAPI.deleteModelById(Number(deletingOrEditingIdRef.current))
            .then(() => {
                setDeleteConfirmationOpen(false);
                deletingOrEditingIdRef.current = undefined;
                fetchModels(filter, order, sort);
            })
            .catch((err) => {
                console.log(err);
            });
    };

    useEffect(() => {
        if (location.pathname !== "/models") return;
        fetchModels(filter, order, sort);
    }, [location.pathname, order, sort]);

    return (
        <>
            <SidebarInset id="sidebar-inset" className="p-2 flex flex-col w-full">
                <div className=" flex items-center justify-between border-b">
                    <SidebarTrigger/>
                    {
                        <h1 className=" p-2 pb-3 text-3xl font-bold tracking-tight first:mt-0">{`Models`}</h1>
                    }
                    <ThemeToggler></ThemeToggler>
                </div>
                <div className="grow flex flex-col">
                    {
                        <div className="grid grid-cols-3 mt-3 justify-between mx-8">
                            <div></div>
                            <div className="flex gap-2 w-full items-center">
                                <div className="flex flex-col w-full items-center gap-2">
                                    <div className="text-center text-base font-semibold">
                                        Car availability range
                                    </div>
                                    <div className="w-full flex gap-8">
                                        <div className="w-1/2">
                                            <DateTimePicker
                                                calendarDisabled={(val) => {
                                                    return date?.to ? val > date.to : val < new Date();
                                                }}
                                                defaultPopupValue={date?.from ? date.from : new Date()}
                                                placeholder="From"
                                                value={date?.from}
                                                onChange={(value) => {
                                                    setDate({from: value, to: date?.to});
                                                    if (value && date?.to) {
                                                        fetchModels(filter, order, sort, 0, undefined, {
                                                            from: value,
                                                            to: date?.to,
                                                        });
                                                    }
                                                }}
                                                granularity="minute"
                                            />
                                        </div>
                                        <div className="w-1/2">
                                            <DateTimePicker
                                                calendarDisabled={(val) => {
                                                    return date?.from
                                                        ? val < date.from
                                                        : val < new Date();
                                                }}
                                                defaultPopupValue={
                                                    date?.to
                                                        ? date.to
                                                        : date?.from
                                                            ? date.from
                                                            : undefined
                                                }
                                                placeholder="To"
                                                value={date?.to}
                                                onChange={(value) => {
                                                    setDate({from: date?.from, to: value});
                                                    if (date?.from && value) {
                                                        fetchModels(filter, order, sort, 0, undefined, {
                                                            from: date?.from,
                                                            to: value,
                                                        });
                                                    }
                                                }}
                                                granularity="minute"
                                            />
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div className="flex gap-3 justify-end items-end">
                                {!filtersSidebarOpen && (
                                    <Select value={sort} onValueChange={setSort}>
                                        <SelectTrigger className="w-[200px] overflow-x-clip">
                                            <div className="flex items-center w-full">
                        <span className="material-symbols-outlined md-18 mr-2">
                          swap_vert
                        </span>
                                                <SelectValue placeholder="Order by" className="pl-8"/>
                                            </div>
                                        </SelectTrigger>
                                        <SelectContent>
                                            <SelectGroup>
                                                <RadioGroup
                                                    orientation="horizontal"
                                                    value={order}
                                                    onValueChange={setOrder}
                                                    defaultValue="asc"
                                                    className="flex justify-between m-2">
                                                    <div className="flex items-center space-x-2">
                                                        <RadioGroupItem value="asc" id="asc"/>
                                                        <Label htmlFor="asc">Asc</Label>
                                                    </div>
                                                    <div className="flex items-center space-x-2">
                                                        <RadioGroupItem value="desc" id="desc"/>
                                                        <Label htmlFor="desc">Desc</Label>
                                                    </div>
                                                </RadioGroup>
                                            </SelectGroup>
                                            <SelectSeparator></SelectSeparator>
                                            <SelectGroup>
                                                <SelectItem value="brand">Brand</SelectItem>
                                                <SelectItem value="model">Model</SelectItem>
                                                <SelectItem value="year">Year</SelectItem>
                                                <SelectItem value="segment">Segment</SelectItem>
                                                <SelectItem value="category">Category</SelectItem>
                                                <SelectItem value="engineType">EngineType</SelectItem>
                                                <SelectItem value="transmissionType">
                                                    Transmission Type
                                                </SelectItem>
                                                <SelectItem value="drivetrain">Drivetrain</SelectItem>
                                            </SelectGroup>
                                        </SelectContent>
                                    </Select>
                                )}
                            </div>
                        </div>
                    }
                    <div className="grid grid-cols-3 mt-8 justify-between mx-8">
                        <div className="flex gap-2 items-center">
                            <Tabs
                                defaultValue="undefined"
                                value={filter.engineType || "undefined"}
                                onValueChange={(value) => {
                                    const newFilter = {
                                        ...filter,
                                        engineType:
                                            value !== "undefined" ? (value as EngineType) : undefined,
                                    };
                                    fetchModels(newFilter, order, sort, 0);
                                    setFilter(newFilter);
                                }}
                                className="w-[300px] p-0">
                                <TabsList className="p-1">
                                    <TabsTrigger value="undefined">All</TabsTrigger>
                                    <TabsTrigger value="PETROL">Petrol</TabsTrigger>
                                    <TabsTrigger value="DIESEL">Diesel</TabsTrigger>
                                    <TabsTrigger value="ELECTRIC">Electric</TabsTrigger>
                                    <TabsTrigger value="HYBRID">Hybrid</TabsTrigger>
                                </TabsList>
                            </Tabs>
                        </div>
                        <div className="flex gap-3  items-center">
                            <Input
                                id="searchBar"
                                className="h-10"
                                startIcon={
                                    <span className="material-symbols-outlined items-center md-18">
                    search
                  </span>
                                }
                                placeholder={"Search by model or brand"}
                                value={filter.search || ""}
                                onChange={(event) => {
                                    const value = event.target.value;
                                    setFilter({...filter, search: value});
                                }}
                                //onKeyDown={() => fetchModels(filter)}
                            >

                            </Input>
                            <Button
                                size="icon"
                                variant="default"
                                onClick={() => fetchModels(filter)}>
                                <span className="material-symbols-outlined md-18">search</span>
                            </Button>
                        </div>
                        <div className="flex gap-3 justify-end items-end">
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
                            {user && user.role !== UserRole.CUSTOMER && (
                                <Button
                                    variant="default"
                                    size="lg"
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        deletingOrEditingIdRef.current = undefined;
                                        navigate("add");
                                    }}>
                                    <span className="material-symbols-outlined md-18">add</span>
                                    Create model
                                </Button>
                            )}
                        </div>
                    </div>
                    <div
                        className={
                            "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 mb-2 mt-3 gap-y-8 gap-x-2 mx-2 justify-items-center grow"
                        }>
                        {carModels && carModels.length > 0 ? (
                            <>
                                {carModels.map((model) => {
                                    return (
                                        <Card
                                            className="max-w-7/8 h-fit"
                                            key={model.id}
                                            onClick={() => {
                                                navigate(`${model.id}`);
                                            }}>
                                            <CardHeader>
                                                <CardTitle>{`${model.brand} ${model.model} ${model.year}`}</CardTitle>
                                                <CardDescription>{model.segment}</CardDescription>
                                            </CardHeader>
                                            <CardContent className="flex flex-col items-center">
                                                <img
                                                    src={DefaultCar}
                                                    className="w-5/6"
                                                    alt={`${model.brand} ${model.model}`}
                                                />
                                                <div className="flex justify-between w-full">
                                                    <div>
                                                        <h3 className="text-2xl font-bold">
                                                            {model.rentalPrice.toFixed(2)} €{" "}
                                                            <span className="text-sm text-muted-foreground">
                                / day
                              </span>
                                                        </h3>
                                                    </div>

                                                    <div>
                                                        <div className="flex gap-2">
                              <span className="material-symbols-outlined align-bottom ">
                                chair
                              </span>
                                                            {model.seatingCapacity} Seats
                                                        </div>
                                                        <div className="flex gap-2">
                              <span className="material-symbols-outlined align-bottom ">
                                door_open
                              </span>
                                                            {model.doorsNumber} Doors
                                                        </div>
                                                    </div>
                                                </div>
                                            </CardContent>
                                            <CardFooter>
                                                <div className="flex gap-3">
                                                    {user && user.role !== UserRole.CUSTOMER ? (
                                                        <>
                                                            <Button
                                                                variant="destructive"
                                                                size="icon"
                                                                onClick={(e) => {
                                                                    e.stopPropagation();
                                                                    deletingOrEditingIdRef.current = model.id;
                                                                    setDeleteConfirmationOpen(true);
                                                                }}>
                                <span className="material-symbols-outlined md-18">
                                  delete
                                </span>
                                                            </Button>
                                                            <Button
                                                                variant="secondary"
                                                                size="icon"
                                                                onClick={(e) => {
                                                                    e.stopPropagation();
                                                                    deletingOrEditingIdRef.current = model.id;
                                                                    navigate("edit");
                                                                }}>
                                <span className="material-symbols-outlined md-18">
                                  edit
                                </span>
                                                            </Button>
                                                        </>
                                                    ) : (
                                                        <Button
                                                            disabled={!date?.from || !date?.to}
                                                            onClick={(e) => {
                                                                e.stopPropagation();
                                                                deletingOrEditingIdRef.current = model.id;
                                                                navigate("reserve");
                                                            }}>
                              <span className="material-symbols-outlined md-18">
                                event_upcoming
                              </span>
                                                            {user ? "Reserve" : "Login to reserve"}
                                                        </Button>
                                                    )}
                                                </div>
                                            </CardFooter>
                                        </Card>
                                    );
                                })}
                                <div className="flex w-full col-span-full items-center mt-auto">
                                    <PaginationWrapper
                                        currentPage={page}
                                        totalPages={totalPages}
                                        onPageChange={(value) => {
                                            fetchModels(filter, order, sort, value);
                                        }}></PaginationWrapper>
                                </div>
                            </>
                        ) : (
                            <div
                                className="w-full col-span-full grow flex items-center justify-center text-2xl font-semibold">
                                No car models found
                            </div>
                        )}
                    </div>
                </div>
                {user && user.role !== UserRole.CUSTOMER && (
                    <ConfirmationDialog
                        open={deleteConfirmationOpen}
                        handleSubmit={handleDelete}
                        title="Delete Confirmation"
                        submitButtonLabel="Delete"
                        submitButtonVariant="destructive"
                        content="Are you sure to delete this model?"
                        description="This action is irreversible"
                        descriptionClassName="text-warning"
                        handleCancel={() => {
                            deletingOrEditingIdRef.current = undefined;
                            setDeleteConfirmationOpen(false);
                        }}></ConfirmationDialog>
                )}
                <Outlet
                    context={
                        user && user.role !== UserRole.CUSTOMER
                            ? deletingOrEditingIdRef.current
                                ? carModels?.find(
                                    (model) => model.id === deletingOrEditingIdRef.current
                                )
                                : undefined
                            : {
                                plannedPickUpDate: date?.from,
                                plannedDropOffDate: date?.to,
                                carModelId: deletingOrEditingIdRef.current,
                            }
                    }></Outlet>
            </SidebarInset>
            {filtersSidebarOpen && (
                <ModelFiltersSidebar
                    mobileOpen={filtersSidebarOpen}
                    setMobileOpen={setFiltersSidebarOpen}
                    filter={filter}
                    fetchModels={(filter, order, sort) =>
                        fetchModels(filter, order, sort, 0)
                    }
                    setFilter={setFilter}></ModelFiltersSidebar>
            )}
        </>
    );
}

export default CarModelsPage;
