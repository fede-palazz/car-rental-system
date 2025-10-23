import NotesAPI from "@/API/NotesAPI";
import ConfirmationDialog from "@/components/ConfirmationDialog";
import { useNavigate, useParams } from "react-router-dom";
import { toast } from "sonner";

function DeleteNoteDialog() {
  const navigate = useNavigate();
  const { vehicleId, noteId } = useParams<{
    vehicleId: string;
    noteId: string;
  }>();
  const handleDelete = () => {
    NotesAPI.deleteNoteById(Number(vehicleId), Number(noteId))
      .then(() => {
        toast.success("Note deleted successfully");
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
      content="Are you sure to delete this note?"
      description="This action is irreversible"
      descriptionClassName="text-warning"
      handleCancel={() => {
        navigate(-1);
      }}></ConfirmationDialog>
  );
}

export default DeleteNoteDialog;
