import CarModelAPI from "@/API/CarModelsAPI";
import ConfirmationDialog from "@/components/ConfirmationDialog";
import { useNavigate, useParams } from "react-router-dom";
import { toast } from "sonner";

function DeleteCarModelDialog() {
  const navigate = useNavigate();
  const { carModelId } = useParams<{
    carModelId: string;
  }>();
  const handleDelete = () => {
    CarModelAPI.deleteModelById(Number(carModelId))
      .then(() => {
        toast.success("Car model deleted successfully");
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
      content="Are you sure to delete this model?"
      description="This action is irreversible"
      descriptionClassName="text-warning"
      handleCancel={() => {
        navigate(-1);
      }}></ConfirmationDialog>
  );
}

export default DeleteCarModelDialog;
