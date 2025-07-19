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
import { CarModel } from "@/models/CarModel.ts";
import CarModelAPI from "@/API/CarModelsAPI";
import { CarSegment } from "@/models/enums/CarSegment";
import { EngineType } from "@/models/enums/EngineType";
import { Drivetrain } from "@/models/enums/Drivetrain";
import { TransmissionType } from "@/models/enums/TransmissionType";
import { defineStepper } from "@/components/ui/stepper";
import ModelInfoForm from "./ModelInfoForm";
import EngineInfoForm from "./EngineInfoForm";
import CarModelDetailsList from "@/components/CarModelDetailsList";
import { CarModelCreateDTO } from "@/models/dtos/CarModelCreateDTO";
import { useNavigate, useOutletContext } from "react-router-dom";
import { CarCategory } from "@/models/enums/CarCategory";
import { toast } from "sonner";

const modelInfoSchema = z.object({
  brand: z.string().min(1, "Brand must not be blank").max(50),
  model: z.string().min(1, "Model must not be blank").max(50),
  year: z
    .string()
    .regex(/^\d{4}$/, "Year must be exactly 4 digits")
    .refine(
      (year) => {
        const numericYear = parseInt(year, 10);
        const currentYear = new Date().getFullYear();
        return numericYear >= 1950 && numericYear <= currentYear;
      },
      { message: `Year must be between 1950 and the current year` }
    ),
  segment: z.nativeEnum(CarSegment, {
    errorMap: (issue) => {
      if (issue.code === "invalid_type") {
        return { message: "Segment is required" };
      }
      return { message: "Invalid segment selected" };
    },
  }),
  doorsNumber: z.coerce
    .number({
      required_error: "Number of doors is required",
      invalid_type_error: "Number of doors must be a number",
    })
    .min(1, "Number of doors must be greater than zero"),
  seatingCapacity: z.coerce
    .number({
      required_error: "Seating capacity is required",
      invalid_type_error: "Seating capacity must be a number",
    })
    .min(1, "Seating capacity must be greater than zero"),
  luggageCapacity: z.coerce
    .number({
      required_error: "Seating capacity is required",
      invalid_type_error: "Seating capacity must be a number",
    })
    .min(1, "Seating capacity must be greater than zero"),
  category: z.nativeEnum(CarCategory, {
    errorMap: (issue) => {
      if (issue.code === "invalid_type") {
        return { message: "Category is required" };
      }
      return { message: "Invalid Category selected" };
    },
  }),

  rentalPrice: z.coerce
    .number({
      required_error: "Rental Price is required",
      invalid_type_error: "Rental Price must be a number",
    })
    .min(1, "Rental Price must be greater than zero"),
});

const engineInfoSchema = z.object({
  engineType: z.nativeEnum(EngineType, {
    errorMap: (issue) => {
      if (issue.code === "invalid_type") {
        return { message: "Engine Type is required" };
      }
      return { message: "Invalid engine type selected" };
    },
  }),
  transmissionType: z.nativeEnum(TransmissionType, {
    errorMap: (issue) => {
      if (issue.code === "invalid_type") {
        return { message: "Transmission Type is required" };
      }
      return { message: "Invalid transmission type selected" };
    },
  }),
  drivetrain: z.nativeEnum(Drivetrain, {
    errorMap: (issue) => {
      if (issue.code === "invalid_type") {
        return { message: "Drivetrain is required" };
      }
      return { message: "Invalid drivetrain selected" };
    },
  }),
  motorDisplacement: z.optional(
    z.coerce
      .number({
        invalid_type_error: "Motor Displacement must be a number",
      })
      .min(1, "Motor Displacement must be greater than zero")
  ),
  featureIds: z.array(z.number()),
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
    id: "model",
    title: "Model",
    schema: modelInfoSchema,
    Component: ModelInfoForm,
  },
  {
    id: "engine",
    title: "Engine",
    schema: engineInfoSchema,
    Component: EngineInfoForm,
  },
  {
    id: "review",
    title: "Review",
    schema: z.object({}),
    Component: CarModelDetailsList,
  }
);

function StepperizedForm({
  model,
  handleSubmit,
  handleCancel,
}: {
  model?: CarModel | undefined;
  handleSubmit: (arg: CarModelCreateDTO) => void;
  handleCancel: () => void;
}) {
  const methods = useStepper();

  const form = useForm<CarModelCreateDTO>({
    resolver: zodResolver(
      methods.current.schema as unknown as z.ZodSchema<CarModelCreateDTO>
    ),
    defaultValues: {
      brand: model ? model.brand : "",
      model: model ? model.model : "",
      year: model ? model.year : "",
      segment: model ? model.segment : undefined,
      doorsNumber: model ? model.doorsNumber : undefined,
      seatingCapacity: model ? model.seatingCapacity : undefined,
      luggageCapacity: model ? model.luggageCapacity : undefined,
      category: model ? model.category : undefined,
      featureIds: model ? model.features.map((feature) => feature.id) : [],
      engineType: model ? model.engineType : undefined,
      transmissionType: model ? model.transmissionType : undefined,
      drivetrain: model ? model.drivetrain : undefined,
      motorDisplacement: model ? model.motorDisplacement : undefined,
      rentalPrice: model ? model.rentalPrice : undefined,
    },
  });

  function onSubmit() {
    handleSubmit(form.getValues());
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
          <DialogTitle>{model ? "Edit Model" : "Create Model"}</DialogTitle>
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
              model: ({ Component }) => (
                <Component
                  control={form.control as unknown as Control}></Component>
              ),
              engine: ({ Component }) => (
                <Component
                  control={form.control as unknown as Control}></Component>
              ),
              review: ({ Component }) => {
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
              },
            })}

            <DialogFooter className="col-span-full">
              <StepperControls className="w-full justify-between">
                <Button
                  variant="secondary"
                  onClick={methods.prev}
                  disabled={methods.isFirst}>
                  <span className="material-symbols-outlined items-center md-18">
                    {model ? "close" : "arrow_back"}
                  </span>
                  {methods.isFirst ? "Cancel" : "Back"}
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
                  {methods.isLast ? (model ? "Edit" : "Create") : "Next"}
                  <span className="material-symbols-outlined  md-18">
                    {methods.isLast
                      ? model
                        ? "edit"
                        : "add"
                      : "arrow_forward"}
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

export default function AddOrEditModelDialog() {
  const navigate = useNavigate();
  const model: CarModel | undefined = useOutletContext();

  const handleEdit = (values: CarModelCreateDTO) => {
    CarModelAPI.editModelById(values, Number(model!.id))
      .then(() => {
        navigate(-1);
      })
      .catch((err) => {
        console.log(err);
      });
  };

  const handleCreate = (values: CarModelCreateDTO) => {
    CarModelAPI.createModel(values)
      .then(() => {
        navigate(-1);
      })
      .catch((err) => {
        console.log(err);
      });
  };

  return (
    <StepperProvider labelOrientation="vertical">
      <StepperizedForm
        model={model}
        handleSubmit={!model ? handleCreate : handleEdit}
        handleCancel={() => navigate(-1)}
      />
    </StepperProvider>
  );
}
