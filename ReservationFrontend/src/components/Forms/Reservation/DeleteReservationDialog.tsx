import ReservationsAPI from "@/API/ReservationsAPI";
import ConfirmationDialog from "@/components/ConfirmationDialog";
import { useNavigate, useParams } from "react-router-dom";
import { toast } from "sonner";

function DeleteReservationDialog() {
  const navigate = useNavigate();
  const { reservationId } = useParams<{
    reservationId: string;
  }>();
  const handleDelete = () => {
    ReservationsAPI.deleteReservationById(Number(reservationId))
      .then(() => {
        toast.success("Reservation deleted successfully");
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
      content="Are you sure to delete this reservation?"
      description="This action is irreversible"
      descriptionClassName="text-warning"
      handleCancel={() => {
        navigate(-1);
      }}></ConfirmationDialog>
  );
}

export default DeleteReservationDialog;
