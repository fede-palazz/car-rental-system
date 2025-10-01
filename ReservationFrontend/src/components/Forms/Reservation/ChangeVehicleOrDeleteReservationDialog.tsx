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
import { Control, useForm } from "react-hook-form";
import { Form } from "@/components/ui/form";
import { defineStepper } from "@/components/ui/stepper";
import { useNavigate, useOutletContext } from "react-router-dom";
import { Reservation } from "@/models/Reservation";
import ChangeVehicleOrDeleteReservationForm from "./ChangeVehicleOrDeleteReservationForm";
import AddOrEditMaintenanceForm from "../Maintenance/AddOrEditMaintenanceForm";

const editVehicleSchema = z.object({
  reservationId: z.number({
    required_error: "Reservation is required",
    invalid_type_error: "Reservation id must be a number",
  }),
  vehicleId: z.number({
    required_error: "Vehicle is required",
    invalid_type_error: "Vehicle id must be a number",
  }),
});

const maintenanceSchema = z.object({
  defects: z.string().min(1, "Defects must not be blank"),
  type: z.string().min(1, "Type must not be blank"),
  upcomingServiceNeeds: z.string().optional(),
  completed: z.boolean(),
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
  reservation: Reservation;
  handleCancel: () => void;
}) {
  const methods = useStepper();

  const form = useForm({
    resolver: zodResolver(methods.current.schema as z.ZodTypeAny),
    defaultValues: {
      reservationId: reservation.id,
      oldVehicleId: reservation.vehicleId,
      newVehicleId: undefined,
      defects: "",
      type: "",
      completed: false,
      upcomingServiceNeeds: "",
    },
  });

  function onSubmit() {}

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
          <DialogTitle>{"Edit Reservation"}</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-4">
            <StepperNavigation className="col-span-full">
              {methods.all.map((step) => (
                <StepperStep
                  key={step.id}
                  of={step.id}
                  type="button"
                  onClick={async (event) => {
                    // todo just return
                    event.stopPropagation();
                    const valid = await form.trigger();
                    //if (!valid) return;
                    methods.goTo(step.id);
                  }}>
                  <StepperTitle>{step.title}</StepperTitle>
                </StepperStep>
              ))}
            </StepperNavigation>
            {methods.switch({
              reservation: ({ Component }) => (
                <Component
                  control={form.control as unknown as Control}></Component>
              ),
              maintenance: ({ Component }) => (
                <Component control={form.control}></Component>
              ),
              /*review: ({ Component }) => {
                const { featureIds, ...rest } =
                  form.getValues() as CarModelCreateDTO;

                const reviewData: CarModel = {
                  ...rest,
                  features: featureIds.map((id) => {
                    return {
                      id: id,
                      description: "",
                    };
                  }),
                  id: 0,
                };
                return (
                  <div className="flex w-full col-span-full">
                    <Component model={reviewData} form></Component>
                  </div>
                );
              },*/
            })}

            <DialogFooter className="col-span-full">
              <StepperControls className="w-full justify-between">
                <Button
                  variant="secondary"
                  onClick={methods.prev}
                  disabled={methods.isFirst}>
                  <span className="material-symbols-outlined items-center md-18">
                    {"close"}
                  </span>
                  {"Cancel"}
                </Button>

                <Button
                  variant="default"
                  type="button"
                  onClick={(event) => {
                    event.stopPropagation();
                    if (methods.isLast) {
                      onSubmit();
                      return;
                    }
                    methods.beforeNext(async () => {
                      const valid = await form.trigger();
                      if (!valid) {
                        return false;
                      }
                      if (!valid) return false;
                      return true;
                    });
                  }}>
                  {
                    /*methods.isLast ? (model ? "Edit" : "Create") : "Next"*/ "CIAO"
                  }
                  <span className="material-symbols-outlined  md-18">
                    {
                      /*methods.isLast
                      ? model
                        ? "edit"
                        : "add"
                      : "arrow_forward"*/ "edit"
                    }
                  </span>
                </Button>
              </StepperControls>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}

export default function ChangeVehicleOrDeleteReservationDialog() {
  const navigate = useNavigate();
  const reservation: Reservation = useOutletContext();

  const handleEdit = (/*values: CarModelCreateDTO*/) => {
    /*CarModelAPI.editModelById(values, Number(model!.id))
      .then(() => {
        navigate(-1);
      })
      .catch((err) => {
        console.log(err);
      });*/
  };

  const handleCreate = (/*values: CarModelCreateDTO*/) => {
    /*CarModelAPI.createModel(values)
      .then(() => {
        navigate(-1);
      })
      .catch((err) => {
        console.log(err);
      });*/
  };

  return (
    <StepperProvider labelOrientation="vertical">
      <StepperizedForm
        reservation={reservation}
        handleCancel={() => navigate(-1)}
      />
    </StepperProvider>
  );
}
