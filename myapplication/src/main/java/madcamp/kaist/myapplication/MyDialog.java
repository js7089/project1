package madcamp.kaist.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

public class MyDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("연락처 편집")
                .setPositiveButton("수정", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getContext(),"YES",Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("삭제", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getContext(),"NO",Toast.LENGTH_SHORT).show();
                    }
                })
        .setView(getActivity().getLayoutInflater().inflate(R.layout.multitab_tmp, null));
        // Create the AlertDialog object and return it
        return builder.create();
    }
}