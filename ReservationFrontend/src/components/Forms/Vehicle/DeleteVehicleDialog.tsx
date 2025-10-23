import VehicleAPI from "@/API/VehiclesAPI";
import ConfirmationDialog from "@/components/ConfirmationDialog";
import { useNavigate, useParams } from "react-router-dom";
import { toast } from "sonner";

function DeleteVehicleDialog() {
  const navigate = useNavigate();
  const { vehicleId } = useParams<{
    vehicleId: string;
  }>();
  const handleDelete = () => {
    VehicleAPI.deleteVehicleById(Number(vehicleId))
      .then(() => {
        toast.success("Vehicle deleted successfully");
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
      content="Are you sure to delete this vehicle?"
      description="This action is irreversible"
      descriptionClassName="text-warning"
      handleCancel={() => {
        navigate(-1);
      }}></ConfirmationDialog>
  );
}

export default DeleteVehicleDialog;
