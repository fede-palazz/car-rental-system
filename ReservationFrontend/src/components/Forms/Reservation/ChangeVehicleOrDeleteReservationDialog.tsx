import {
  Dialog,
  DialogContent,
  DialogTitle,
  DialogHeader,
  DialogFooter,
  DialogDescription,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { Form } from "@/components/ui/form";
import { defineStepper } from "@/components/ui/stepper";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { Reservation } from "@/models/Reservation";
import ChangeVehicleOrDeleteReservationForm from "./ChangeVehicleOrDeleteReservationForm";
import AddOrEditMaintenanceForm from "../Maintenance/AddOrEditMaintenanceForm";
import { useEffect, useState } from "react";
import MaintenancesAPI from "@/API/MaintenancesAPI";
import { MaintenanceReqDTO } from "@/models/dtos/request/MaintenanceReqDTO";
import ReservationsAPI from "@/API/ReservationsAPI";
import { toast } from "sonner";
import { Spinner } from "@/components/ui/spinner";

const editVehicleSchema = z.object({
  newVehicleId: z.number({
    required_error: "Vehicle is required",
    invalid_type_error: "Vehicle id must be a number",
  }),
});

const maintenanceSchema = z.object({
  startDate: z
    .date({
      required_error: "Start Date is required",
      invalid_type_error: "Start Date must be a valid date",
    })
    .refine((date) => date >= new Date(), {
      message: "Start Date cannot be in the past",
    }),
  plannedEndDate: z
    .date({
      required_error: "Planned End Date is required",
      invalid_type_error: "Planned End Date must be a valid date",
    })
    .refine((date) => date >= new Date(), {
      message: "Planned End Date cannot be in the past",
    }),
  defects: z.string().min(1, "Defects must not be blank"),
  type: z.string().min(1, "Type must not be blank"),
  upcomingServiceNeeds: z.string(),
});

const {
  StepperProvider,
  StepperControls,
  StepperNavigation,
  StepperStep,
  StepperTitle,
  useStepper,
} = defineStepper(
  {
    id: "reservation",
    title: "Reservation",
    schema: editVehicleSchema,
    Component: ChangeVehicleOrDeleteReservationForm,
  },
  {
    id: "maintenance",
    title: "Maintenance",
    schema: maintenanceSchema,
    Component: AddOrEditMaintenanceForm,
  }
);

function StepperizedForm({
  reservation,
  handleCancel,
}: {
  reservation?: Reservation;
  handleCancel: () => void;
}) {
  const methods = useStepper();

  const form = useForm({
    resolver: zodResolver(methods.current.schema as z.ZodTypeAny),
    defaultValues: {
      newVehicleId: undefined,
      //Maintenance
      startDate: new Date(),
      plannedEndDate: undefined,
      defects: "",
      type: "",
      upcomingServiceNeeds: "",
    },
  });
  const navigate = useNavigate();
  const [deleting, setDeleting] = useState<boolean>(false);

  function handleChangeReservationVehicle() {
    if (!reservation) return;
    const newVehicleId = form.getValues("newVehicleId");
    ReservationsAPI.updateReservationVehicle(reservation.id, newVehicleId)
      .then(() => {
        toast.success("Reservation changed successfully");
        methods.next();
      })
      .catch((err: Error) => {
        toast.error(err.message);
        navigate(-1);
      });
  }

  function handleDeleteReservation() {
    if (!reservation) return;
    ReservationsAPI.deleteReservationById(reservation.id)
      .then(() => {
        toast.success("Reservation deleted successfully");
        methods.next();
      })
      .catch((err: Error) => {
        toast.error(err.message);
        navigate(-1);
      });
  }

  function handleMaintenanceCreate() {
    if (!reservation) return;
    const oldVehicleId = reservation?.vehicleId;
    //Maintenance
    const startDate = form.getValues("startDate");
    const plannedEndDate = form.getValues("plannedEndDate");
    const defects = form.getValues("defects");
    const type = form.getValues("type");
    const upcomingServiceNeeds = form.getValues("upcomingServiceNeeds");

    const reqDTO: MaintenanceReqDTO = {
      startDate,
      plannedEndDate,
      defects,
      type,
      upcomingServiceNeeds,
    };
    MaintenancesAPI.createMaintenance(oldVehicleId, reqDTO)
      .then(() => {
        toast.success("Maintenance created successfully");
        navigate(-1);
      })
      .catch((err: Error) => {
        toast.error(err.message);
      });
  }

  return (
    <Dialog
      open={true}
      onOpenChange={(isOpen) => {
        if (!isOpen) handleCancel();
      }}>
      <DialogDescription></DialogDescription>
      <DialogContent
        className="sm:max-w-[425px] md:min-w-1/2 overflow-auto max-h-11/12"
        style={{
          scrollbarWidth: "none", // For Firefox
          msOverflowStyle: "none", // For IE and Edge
        }}>
        <DialogHeader>
          <DialogTitle>
            {methods.isFirst ? "Edit Reservation's Vehicle" : "Add Maintenance"}
          </DialogTitle>
        </DialogHeader>
        {!reservation ? (
          <div className="flex items-center justify-center w-full min-h-[300px]">
            <Spinner />
          </div>
        ) : (
          <Form {...form}>
            <form className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-4">
              <StepperNavigation className="col-span-full">
                {methods.all.map((step) => (
                  <StepperStep
                    key={step.id}
                    of={step.id}
                    type="button"
                    onClick={async (event) => {
                      event.stopPropagation();
                      const valid = await form.trigger();
                      if (!valid) return;
                      methods.goTo(step.id);
                    }}>
                    <StepperTitle>{step.title}</StepperTitle>
                  </StepperStep>
                ))}
              </StepperNavigation>
              {methods.switch({
                reservation: ({ Component }) => (
                  <>
                    <Component
                      control={form.control}
                      currentVehicleId={reservation.vehicleId}
                      onVehicleSelection={(newVehicleId) => {
                        form.setValue("newVehicleId", newVehicleId);
                      }}
                      carModelId={reservation.carModelId}
                      desiredStartDate={reservation.plannedPickUpDate}
                      desiredEndDate={
                        reservation.plannedDropOffDate
                      }></Component>
                    <div className="text-lg text-center col-span-full w-full font-semibold text-warning">
                      {deleting &&
                        "Are you absolutely sure to delete the reservation?"}
                    </div>
                  </>
                ),
                maintenance: ({ Component }) => (
                  <Component control={form.control}></Component>
                ),
              })}
              <DialogFooter className="col-span-full">
                <StepperControls className="w-full justify-between">
                  <Button
                    variant="secondary"
                    type="button"
                    onClick={(event) => {
                      event.stopPropagation();
                      handleCancel();
                    }}>
                    <span className="material-symbols-outlined items-center md-18">
                      {"close"}
                    </span>
                    {"Cancel"}
                  </Button>
                  <div className="flex flex-row gap-2">
                    {methods.isFirst && (
                      <Button
                        variant="destructive"
                        type="button"
                        onClick={(event) => {
                          event.stopPropagation();
                          if (deleting) {
                            handleDeleteReservation();
                          } else {
                            setDeleting(true);
                          }
                        }}>
                        Delete
                        <span className="material-symbols-outlined  md-18">
                          delete
                        </span>
                      </Button>
                    )}
                    <Button
                      variant="default"
                      type="button"
                      onClick={async (event) => {
                        event.stopPropagation();
                        const valid = await form.trigger();
                        if (!valid) return;
                        if (methods.isLast) {
                          handleMaintenanceCreate();
                          return;
                        } else {
                          handleChangeReservationVehicle();
                        }
                      }}
                      disabled={form.watch("newVehicleId") == undefined}>
                      {methods.isFirst ? "Change" : "Create"}
                      <span className="material-symbols-outlined  md-18">
                        {methods.isFirst ? "swap_driving_apps" : "add"}
                      </span>
                    </Button>
                  </div>
                </StepperControls>
              </DialogFooter>
            </form>
          </Form>
        )}
      </DialogContent>
    </Dialog>
  );
}

export default function ChangeVehicleOrDeleteReservationDialog() {
  const navigate = useNavigate();
  const location = useLocation();
  const { reservationId } = useParams<{
    reservationId: string;
  }>();
  const [reservation, setReservation] = useState<Reservation | undefined>(
    undefined
  );

  useEffect(() => {
    //if (location.pathname !== `/reservations/${vehicleId}`) return;
    ReservationsAPI.getReservationById(Number(reservationId))
      .then((reservation: Reservation) => {
        console.log(reservation);
        setReservation(reservation);
      })
      .catch((err: Error) => {
        toast.error(err.message);
      });
  }, [reservationId, location.pathname]);

  return (
    <StepperProvider labelOrientation="vertical">
      <StepperizedForm
        reservation={reservation}
        handleCancel={() => navigate(-1)}
      />
    </StepperProvider>
  );
}
