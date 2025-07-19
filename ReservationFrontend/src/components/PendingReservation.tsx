import { cn } from "@/lib/utils";
import { Reservation } from "@/models/Reservation";
import { useEffect, useState } from "react";
import DefaultCar from "@/assets/defaultCarModel.png";
import PaypalLogo from "@/assets/paypal.svg";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "./ui/card";
import { Separator } from "./ui/separator";
import { format } from "date-fns";
import { Button } from "./ui/button";
import ReservationsAPI from "@/API/ReservationsAPI";

function PendingReservation({
  reservation,
  handleCancel,
}: {
  reservation: Reservation;
  handleCancel: (e: React.MouseEvent<HTMLButtonElement>) => void;
}) {
  // Calculate the difference in minutes between now and the reservation creation date
  const minutesSinceCreation = Math.floor(
    (new Date().getTime() - reservation.creationDate.getTime()) / 60000
  );
  const [expirationTimer, setExpirationTimer] = useState<number>(
    30 * 60 - minutesSinceCreation * 60
  );

  useEffect(() => {
    if (expirationTimer <= 0) return;

    const timer = setTimeout(() => {
      setExpirationTimer((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearTimeout(timer);
  }, [expirationTimer]);

  return (
    <div className="flex flex-col w-full mb-6 mt-8 px-8">
      <p className="text-2xl font-bold text-center">
        You have a pending reservation
      </p>
      <p className="text-base font-semibold mt-3 mb-1">
        Your reservation will be active for{" "}
        <span
          className={cn(
            "text-lg",
            expirationTimer / 60 < 5 ? "text-destructive" : "text-info"
          )}>
          {Math.floor(expirationTimer / 60)
            .toString()
            .padStart(2, "0")}
          :{(expirationTimer % 60).toString().padStart(2, "0")}{" "}
        </span>
        minutes, pay it before it expires
      </p>
      <Card>
        <CardHeader className="font-semibold text-lg flex justify-between items-center">
          <CardTitle className="flex items-center gap-2 text-lg">{`${reservation.brand} ${reservation.model} ${reservation.year}`}</CardTitle>
          <CardDescription className="items-center flex gap-1">
            <span className="material-symbols-outlined md-18">event</span>
            {format(reservation.creationDate, "dd/MM/yyyy")}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col md:flex-row items-center gap-6 h-full">
            <div className="h-full flex w-1/2 flex-col">
              <div className="flex-grow flex items-center justify-center">
                <img
                  src={DefaultCar}
                  className="py-4"
                  alt={`${reservation.brand} ${reservation.model} ${reservation.year}`}
                />
              </div>
            </div>
            <Separator
              orientation="vertical"
              className="hidden md:block"></Separator>
            <Separator
              orientation="horizontal"
              className="block md:hidden"></Separator>
            <div className="flex flex-col h-full mt-2 py-2 w-full">
              <ul className="grid grid-cols-2 lg:grid-cols-2 gap-y-4 w-full justify-items-center text-md">
                <li className="w-full grid grid-cols-3 gap-x-4 ">
                  <div className="flex justify-end items-center col-span-1">
                    <span className="material-symbols-outlined md-18 rounded-full bg-sidebar-accent p-2.5">
                      tag
                    </span>
                  </div>
                  <div className="space-y-1 text-start col-span-2">
                    <p>
                      <strong className="text-accent-foreground text-lg">
                        License plate
                      </strong>
                    </p>
                    {reservation.licensePlate}
                  </div>
                </li>
                <li className="w-full grid grid-cols-3 gap-x-4 ">
                  <div className="flex justify-end items-center col-span-1">
                    <span className="material-symbols-outlined md-18 rounded-full bg-sidebar-accent p-2.5">
                      adjust
                    </span>
                  </div>
                  <div className="space-y-1 text-start col-span-2">
                    <p>
                      <strong className="text-accent-foreground text-lg">
                        Status
                      </strong>
                    </p>
                    <span>
                      {reservation.status
                        .replace(/_/g, " ")
                        .charAt(0)
                        .toUpperCase() +
                        reservation.status
                          .replace(/_/g, " ")
                          .slice(1)
                          .toLowerCase()}
                    </span>
                  </div>
                </li>
                <li className="w-full grid grid-cols-3 gap-x-4 ">
                  <div className="flex justify-end items-center col-span-1">
                    <span className="material-symbols-outlined md-18 rounded-full bg-sidebar-accent p-2.5">
                      event_upcoming
                    </span>
                  </div>
                  <div className="space-y-1 text-start col-span-2">
                    <p>
                      <strong className="text-accent-foreground text-lg">
                        Planned Pickup date
                      </strong>
                    </p>
                    {format(reservation.plannedPickUpDate, "dd/MM/yyyy")}
                  </div>
                </li>
                <li className="w-full grid grid-cols-3 gap-x-4 ">
                  <div className="flex justify-end items-center col-span-1">
                    <span className="material-symbols-outlined md-18 rounded-full bg-sidebar-accent p-2.5">
                      event_busy
                    </span>
                  </div>
                  <div className="space-y-1 text-start col-span-2">
                    <p>
                      <strong className="text-accent-foreground text-lg">
                        Planned DropOff date
                      </strong>
                    </p>
                    {format(reservation.plannedDropOffDate, "dd/MM/yyyy")}
                  </div>
                </li>
              </ul>
              <div className="grid grid-cols-2 items-center w-full gap-2 mt-8 mb-4">
                <p className="text-xl font-extrabold text-center">

                </p>
                <div className="flex gap-1 justify-center items-center">
                  <div>
                    <Button
                      variant="ghost"

                      onClick={handleCancel}
                      className="text-sm text-destructive hover:text-destructive/60 w-1/2">
                      <span className="material-symbols-outlined md-18">
                        cancel
                      </span>
                      Cancel
                    </Button>
                  </div>
                  <div>
                    <Button
                      size="lg"
                      className="text-lg"
                      onClick={() => {
                        ReservationsAPI.payReservation(reservation.id, 1).then(
                          //TODO change customer Id
                          (url) => {
                            if (url.redirectURL) {
                              window.location.href = url.redirectURL;
                            }
                          }
                        );
                      }}>
                      <img
                        src={PaypalLogo}
                        alt="PayPal"
                        className="h-7 w-auto mr-2 inline"
                      />
                      Pay
                    </Button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

export default PendingReservation;
