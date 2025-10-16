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
import { useNavigate } from "react-router-dom";
import { useEffect } from "react";
import { MaintenanceReqDTO } from "@/models/dtos/request/MaintenanceReqDTO";
import MaintenancesAPI from "@/API/MaintenancesAPI";
import { Maintenance } from "@/models/Maintenance";
import { useParams } from "react-router-dom";
import AddOrEditMaintenanceForm from "./AddOrEditMaintenanceForm";
import { MaintenanceType } from "@/models/enums/MaintenanceType";

const maintenanceSchema = z
  .object({
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
    type: z.nativeEnum(MaintenanceType, {
      errorMap: (issue) => {
        if (issue.code === "invalid_type") {
          return { message: "Maintenance Type Type is required" };
        }
        return { message: "Invalid maintenance type selected" };
      },
    }),
    upcomingServiceNeeds: z.string(),
  })
  .refine((data) => data.plannedEndDate > data.startDate, {
    path: ["plannedEndDate"],
    message: "End date must be after start date",
  });

export default function AddOrEditMaintenanceDialog() {
  const navigate = useNavigate();
  const { vehicleId, maintenanceId } = useParams<{
    vehicleId: string;
    maintenanceId?: string;
  }>();

  const form = useForm<MaintenanceReqDTO>({
    resolver: zodResolver(maintenanceSchema),
    defaultValues: {
      startDate: new Date(),
      plannedEndDate: undefined,
      defects: "",
      type: undefined,
      upcomingServiceNeeds: "",
    },
  });

  useEffect(() => {
    if (!maintenanceId) return;
    MaintenancesAPI.getMaintenanceById(Number(vehicleId), Number(maintenanceId))
      .then((maintenance: Maintenance) => {
        form.reset(maintenance);
      })
      .catch();
  }, [form, maintenanceId, vehicleId]);

  async function onSubmit() {
    const valid = await form.trigger();
    if (!valid) {
      return;
    }
    if (maintenanceId !== undefined) {
      handleEdit(form.getValues());
    } else {
      handleCreate(form.getValues());
    }
  }

  const handleEdit = (values: MaintenanceReqDTO) => {
    MaintenancesAPI.editMaintenanceById(
      Number(vehicleId),
      values,
      Number(maintenanceId)
    )
      .then(() => {
        navigate(-1);
      })
      .catch((err) => {
        console.log(err);
      });
  };

  const handleCreate = (values: MaintenanceReqDTO) => {
    MaintenancesAPI.createMaintenance(Number(vehicleId), values)
      .then(() => {
        navigate(-1);
      })
      .catch((err) => {
        console.log(err);
      });
  };

  return (
    <Dialog
      open={true}
      onOpenChange={(isOpen) => {
        if (!isOpen) navigate(-1);
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
            {maintenanceId === undefined
              ? "Add Maintenance"
              : "Edit Maintenance"}
          </DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-4">
            <AddOrEditMaintenanceForm
              control={form.control as unknown as Control}
              vehicleId={Number(vehicleId)}></AddOrEditMaintenanceForm>
            <DialogFooter className="col-span-full ">
              <div className="flex items-center justify-between w-full">
                <Button
                  variant="secondary"
                  onClick={(e) => {
                    e.preventDefault();
                    navigate(-1);
                  }}>
                  <span className="material-symbols-outlined items-center md-18">
                    close
                  </span>
                  Cancel
                </Button>
                <Button
                  variant="default"
                  type="button"
                  onClick={(event) => {
                    event.stopPropagation();
                    onSubmit();
                  }}>
                  {maintenanceId === undefined ? "Add" : "Edit"}
                  <span className="material-symbols-outlined  md-18">
                    {maintenanceId === undefined ? "add" : "edit"}
                  </span>
                </Button>
              </div>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
