import MaintenancesAPI from "@/API/MaintenancesAPI";
import ConfirmationDialog from "@/components/ConfirmationDialog";
import { useNavigate, useParams } from "react-router-dom";
import { toast } from "sonner";

function DeleteMaintenanceDialog() {
  const navigate = useNavigate();
  const { vehicleId, maintenanceId } = useParams<{
    vehicleId: string;
    maintenanceId: string;
  }>();
  const handleDelete = () => {
    MaintenancesAPI.deleteMaintenanceById(
      Number(vehicleId),
      Number(maintenanceId)
    )
      .then(() => {
        toast.success("Maintenance deleted successfully");
        navigate(-1);
      })
      .catch((err) => {
        toast.error(err);
      });
  };

  return (
    <ConfirmationDialog
      open={true}
      handleSubmit={handleDelete}
      title="Delete Confirmation"
      submitButtonLabel="Delete"
      submitButtonVariant="destructive"
      content="Are you sure to delete this maintenance?"
      description="This action is irreversible"
      descriptionClassName="text-warning"
      handleCancel={() => {
        navigate(-1);
      }}></ConfirmationDialog>
  );
}

export default DeleteMaintenanceDialog;
