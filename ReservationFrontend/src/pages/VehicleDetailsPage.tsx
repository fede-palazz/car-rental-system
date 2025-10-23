import { Outlet, useLocation, useNavigate, useParams } from "react-router-dom";
import DefaultCar from "@/assets/defaultCarModel.png";
import { useEffect, useState } from "react";
import { Separator } from "@/components/ui/separator";
import { Button } from "@/components/ui/button";
import VehicleAPI from "@/API/VehiclesAPI";
import { Vehicle } from "@/models/Vehicle";
import { SidebarInset, SidebarTrigger } from "@/components/ui/sidebar";
import { ThemeToggler } from "@/components/ThemeToggler";
import VehicleDetailsList from "@/components/VehicleDetailsList";
import { Maintenance } from "@/models/Maintenance";
import { Note } from "@/models/Note";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import {
  Carousel,
  CarouselContent,
  CarouselItem,
  CarouselNext,
  CarouselPrevious,
} from "@/components/ui/carousel";
import MaintenancesAPI from "@/API/MaintenancesAPI";
import { PagedResDTO } from "@/models/dtos/response/PagedResDTO";
import NotesAPI from "@/API/NotesAPI";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
} from "@/components/ui/card";
import { format } from "date-fns";

function VehicleDetailsPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { vehicleId } = useParams<{
    vehicleId: string;
  }>();
  const [vehicle, setVehicle] = useState<Vehicle | undefined>(undefined);
  const [maintenances, setMaintenances] = useState<Maintenance[]>([]);
  const [notes, setNotes] = useState<Note[]>([]);

  const fetchMaintenancesAndNotes = (vehicleId: number) => {
    MaintenancesAPI.getMaintenancesByVehicleId(vehicleId).then(
      (res: PagedResDTO<Maintenance>) => {
        setMaintenances(res.content);
      }
    );
    NotesAPI.getNotesByVehicleId(vehicleId).then((res: PagedResDTO<Note>) => {
      setNotes(res.content);
    });
  };

  useEffect(() => {
    if (location.pathname !== `/vehicles/${vehicleId}`) return;
    VehicleAPI.getVehicleById(Number(vehicleId))
      .then((vehicle: Vehicle) => {
        setVehicle(vehicle);
        fetchMaintenancesAndNotes(vehicle.id);
      })
      .catch((err) => {
        console.log(err);
      });
  }, [vehicleId, location.pathname]);

  return (
    <SidebarInset id="sidebar-inset" className=" pt-2 flex flex-col w-full">
      <div className="px-2 flex items-center justify-between border-b">
        <SidebarTrigger />
        {vehicle && (
          <h1 className=" p-2 pb-3 text-3xl font-bold tracking-tight first:mt-0">
            {`${vehicle?.brand} ${vehicle?.model} ${vehicle?.year} ${vehicle?.vin}`}
          </h1>
        )}
        <ThemeToggler></ThemeToggler>
      </div>
      {vehicle && (
        <div className="grow flex flex-col">
          <div className="flex flex-col md:flex-row items-center lg:h-full">
            <div className=" md:sticky w-1/2 h-full flex flex-col">
              <div className="mt-3">
                <Button
                  variant="ghost"
                  size="icon"
                  onClick={() => navigate(-1)}>
                  <span className="material-symbols-outlined md-18">
                    arrow_back
                  </span>
                </Button>
              </div>
              <div className="flex-grow flex items-center justify-center">
                <img
                  src={DefaultCar}
                  className="py-4"
                  alt={`${vehicle.brand} ${vehicle.model}`}
                />
              </div>
            </div>
            <Separator
              orientation="vertical"
              className="hidden md:block"></Separator>
            <Separator
              orientation="horizontal"
              className="block md:hidden"></Separator>
            <div className="flex flex-col min-h-full max-h-full pt-2 w-full h-full overflow-auto">
              <h2 className="text-3xl text-center font-extrabold">Details</h2>
              <VehicleDetailsList vehicle={vehicle}></VehicleDetailsList>
              <div className="grid grid-cols-2 items-center h-full justify-center w-full gap-4 mt-8">
                <Button
                  variant="destructive"
                  size="lg"
                  className="w-1/2 justify-self-center"
                  onClick={() => navigate("delete")}>
                  <span className="material-symbols-outlined md-18">
                    delete
                  </span>
                  Delete
                </Button>
                <Button
                  variant="default"
                  size="lg"
                  className="w-1/2 justify-self-center"
                  onClick={() => navigate("edit")}>
                  <span className="material-symbols-outlined md-18">edit</span>
                  Edit
                </Button>
              </div>
              <Separator
                orientation="horizontal"
                className="block mt-4"></Separator>
              <div className="flex flex-col grow xl:flex-row overflow-clip items-center w-full h-full gap-4">
                <div className="h-full w-full xl:w-1/2 flex flex-col gap-3 pb-3">
                  <div className="flex gap-2 mt-2 justify-center text-center items-center">
                    <h3 className="text-xl text-center font-bold">
                      Maintenances
                    </h3>
                    <Button
                      variant="outline"
                      onClick={() => navigate("add-maintenance")}
                      className="w-6 h-6 min-w-0 min-h-0 p-0">
                      <span className="material-symbols-outlined md-18">
                        add
                      </span>
                    </Button>
                  </div>
                  <Carousel className="flex items-center grow justify-center w-full">
                    {maintenances.length > 0 && (
                      <CarouselPrevious variant="ghost" />
                    )}
                    <CarouselContent className="flex flex-grow h-full">
                      {maintenances.length > 0 ? (
                        maintenances.map((maintenance) => {
                          return (
                            <CarouselItem key={maintenance.id}>
                              <Card className="h-full py-4">
                                <CardHeader className="flex-col items-center">
                                  <div className="flex justify-between align-middle">
                                    <CardTitle className="text-lg flex items-center gap-2">
                                      <span className="material-symbols-outlined">
                                        build
                                      </span>
                                      {maintenance.type
                                        .charAt(0)
                                        .toUpperCase() +
                                        maintenance.type.slice(1).toLowerCase()}
                                    </CardTitle>
                                    <CardDescription className="flex flex-col gap-1">
                                      <div className="flex items-end gap-1">
                                        <span className="material-symbols-outlined md-18">
                                          event
                                        </span>
                                        {"Start: " +
                                          format(
                                            maintenance.startDate,
                                            "dd/MM/yyyy"
                                          )}
                                      </div>
                                      <div className="flex items-end gap-1">
                                        <span className="material-symbols-outlined md-18">
                                          event
                                        </span>
                                        {"Planned end: " +
                                          format(
                                            maintenance.plannedEndDate,
                                            "dd/MM/yyyy"
                                          )}
                                      </div>
                                    </CardDescription>
                                  </div>
                                  <Badge
                                    variant="outline"
                                    className={cn(
                                      "px-2 align-center py-1",
                                      maintenance.actualEndDate
                                        ? "bg-green-100 text-green-700 border-green-300"
                                        : "bg-red-100 text-red-700 border-red-300"
                                    )}>
                                    <span className="material-symbols-outlined text-inherit-size md-18 mr-1 align-middle">
                                      {maintenance.actualEndDate
                                        ? "check_circle"
                                        : "error"}
                                    </span>
                                    {maintenance.actualEndDate
                                      ? `Completed in ${format(
                                          maintenance.actualEndDate,
                                          "dd/MM/yyyy"
                                        )}`
                                      : "Not Completed"}
                                  </Badge>
                                </CardHeader>
                                <CardContent className="space-y-3">
                                  <div className=" space-y-3">
                                    <div className="flex items-center gap-2">
                                      <span className="material-symbols-outlined md-18">
                                        report_problem
                                      </span>
                                      <div>
                                        <p className="font-semibold text-sm">
                                          Defects
                                        </p>
                                        <p className="text-sm max-h-[1.5rem] wrap-anywhere overflow-auto text-muted-foreground">
                                          {maintenance.defects}
                                        </p>
                                      </div>
                                    </div>
                                    <div className="flex items-center gap-2">
                                      <span className="material-symbols-outlined md-18">
                                        build_circle
                                      </span>
                                      <div>
                                        <p className="font-semibold text-sm">
                                          Upcoming Service
                                        </p>
                                        <p className="text-sm max-h-[1.5rem] wrap-anywhere overflow-auto text-muted-foreground">
                                          {maintenance.upcomingServiceNeeds}
                                        </p>
                                      </div>
                                    </div>
                                  </div>
                                  <div className="flex justify-end">
                                    <Button
                                      variant="ghost"
                                      size="icon"
                                      className=" justify-self-center"
                                      onClick={() => {
                                        navigate(
                                          `delete-maintenance/${maintenance.id}`
                                        );
                                      }}>
                                      <span className="material-symbols-outlined md-18">
                                        delete
                                      </span>
                                    </Button>
                                    <Button
                                      variant="ghost"
                                      size="icon"
                                      className="justify-self-center"
                                      onClick={() =>
                                        navigate(
                                          `edit-maintenance/${maintenance.id}`
                                        )
                                      }>
                                      <span className="material-symbols-outlined md-18">
                                        edit
                                      </span>
                                    </Button>
                                  </div>
                                </CardContent>
                              </Card>
                            </CarouselItem>
                          );
                        })
                      ) : (
                        <div className="grow flex w-full h-full text-muted-foreground text-center items-center justify-center">
                          No Maintenances found
                        </div>
                      )}
                    </CarouselContent>
                    {maintenances.length > 0 && (
                      <CarouselNext variant="ghost" />
                    )}
                  </Carousel>
                </div>
                <Separator
                  orientation="vertical"
                  className="hidden xl:block"></Separator>
                <Separator
                  orientation="horizontal"
                  className="block xl:hidden"></Separator>
                <div className=" flex w-full xl:w-1/2 flex-col gap-3 pb-3 h-full">
                  <div className="flex gap-2 mt-2 justify-center text-center items-center">
                    <h3 className="text-xl text-center font-bold">Notes</h3>
                    <Button
                      variant="outline"
                      className="w-6 h-6 min-w-0 min-h-0 p-0"
                      onClick={() => navigate("add-note")}>
                      <span className="material-symbols-outlined md-18">
                        add
                      </span>
                    </Button>
                  </div>
                  <Carousel className="flex items-center grow justify-center w-full ">
                    {notes.length > 0 && <CarouselPrevious variant="ghost" />}
                    <CarouselContent className="flex grow h-full">
                      {notes.length > 0 ? (
                        notes.map((note: Note) => {
                          return (
                            <CarouselItem
                              key={note.id}
                              className="h-full max-w-full">
                              <Card className="h-full py-4">
                                <CardHeader className="flex justify-between items-center">
                                  <CardTitle className="flex items-center gap-2 text-lg">
                                    <span className="material-symbols-outlined">
                                      Person
                                    </span>
                                    {note.author}
                                  </CardTitle>
                                  <CardDescription className="items-center flex gap-1">
                                    <span className="material-symbols-outlined md-18">
                                      event
                                    </span>
                                    {format(note.date, "dd/MM/yyyy")}
                                  </CardDescription>
                                </CardHeader>
                                <CardContent className="space-y-3 flex flex-col max-w-full grow">
                                  <div className="flex grow max-w-full items-center">
                                    <p className="h-[8rem] wrap-anywhere overflow-y-auto pr-1 flex items-center">
                                      {note.content}
                                    </p>
                                  </div>
                                  <div className="flex justify-end">
                                    <Button
                                      variant="ghost"
                                      size="icon"
                                      className=" justify-self-center"
                                      onClick={() => {
                                        navigate(`delete-note/${note.id}`);
                                      }}>
                                      <span className="material-symbols-outlined md-18">
                                        delete
                                      </span>
                                    </Button>
                                    <Button
                                      variant="ghost"
                                      size="icon"
                                      className="justify-self-center"
                                      onClick={() =>
                                        navigate(`edit-note/${note.id}`)
                                      }>
                                      <span className="material-symbols-outlined md-18">
                                        edit
                                      </span>
                                    </Button>
                                  </div>
                                </CardContent>
                              </Card>
                            </CarouselItem>
                          );
                        })
                      ) : (
                        <div className="grow flex w-full h-full text-muted-foreground text-center items-center justify-center">
                          No Notes found
                        </div>
                      )}
                    </CarouselContent>
                    {notes.length > 0 && <CarouselNext variant="ghost" />}
                  </Carousel>
                </div>
              </div>
            </div>
          </div>
          <Outlet></Outlet>
        </div>
      )}
    </SidebarInset>
  );
}

export default VehicleDetailsPage;
