import VehicleAPI from "@/API/VehiclesAPI";
import { Vehicle } from "@/models/Vehicle";
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { toast } from "sonner";
import DefaultCar from "@/assets/defaultCarModel.png";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "../ui/card";
import { Separator } from "../ui/separator";
import VehicleDetailsList from "../VehicleDetailsList";
import { Button } from "../ui/button";

function MapVehicleCard() {
  const navigate = useNavigate();
  const vehicleId = Number(useParams().vehicleId);
  const [vehicle, setVehicle] = useState<Vehicle | undefined>();

  useEffect(() => {
    VehicleAPI.getVehicleById(vehicleId)
      .then((vehicle) => {
        setVehicle(vehicle);
      })
      .catch((err: Error) => {
        toast.error(err.message);
        navigate(-1);
      });
  }, [vehicleId]);

  return (
    vehicle && (
      <Card
        className="absolute left-4 top-1/2 -translate-y-1/2 max-w-4/9 h-fit z-10000"
        key={vehicle.id}>
        <CardHeader>
          <CardTitle>{`${vehicle.brand} ${vehicle.model} ${vehicle.year}`}</CardTitle>
          <CardDescription>{vehicle.licensePlate}</CardDescription>
          <Button
            variant="ghost"
            size="icon"
            className="absolute top-2 right-2"
            onClick={() => navigate("/tracking")}
            aria-label="Close">
            <span className="material-symbols-outlined md-18">close</span>
          </Button>
        </CardHeader>
        <CardContent className="flex flex-col items-center">
          <div className=" md:sticky w-1/2 h-full flex flex-col">
            <div className="flex-grow flex items-center justify-center">
              <img
                src={DefaultCar}
                className="py-4"
                alt={`${vehicle.brand} ${vehicle.model}`}
              />
            </div>
          </div>
          <Separator
            orientation="horizontal"
            className="hidden md:block"></Separator>
          <div className="flex flex-col min-h-full max-h-full pt-2 w-full h-full overflow-auto">
            <h2 className="text-3xl text-center font-extrabold">Details</h2>
            <VehicleDetailsList
              isInCard={true}
              vehicle={vehicle}></VehicleDetailsList>
          </div>
        </CardContent>
      </Card>
    )
  );
}

export default MapVehicleCard;
