package lk.kalindu.speechbridge.fragment;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

import lk.kalindu.speechbridge.R;
import lk.kalindu.speechbridge.model.Invoice;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;

import java.io.FileOutputStream;

public class InvoiceFragment extends Fragment {

    private static final String ARG_INVOICE_ID = "invoiceId";
    private Invoice invoice;
    private FirebaseFirestore db;


    public static InvoiceFragment newInstance(String invoiceId) {
        InvoiceFragment fragment = new InvoiceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_INVOICE_ID, invoiceId);
        fragment.setArguments(args);
        return fragment;
    }

    public InvoiceFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_invoice, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            String invoiceId = getArguments().getString(ARG_INVOICE_ID);

            if (invoiceId != null) {
                loadInvoice(invoiceId, view);
            } else {
                Toast.makeText(getContext(), "Invoice ID missing", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Fetch invoice from Firestore
    private void loadInvoice(String invoiceId, View view) {

        db.collection("invoice")
                .document(invoiceId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Toast.makeText(getContext(), "Invoice not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    invoice = doc.toObject(Invoice.class);

                    if (invoice == null) return;

                    bindInvoiceData(view);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ui bind
    @SuppressLint("SetTextI18n")
    private void bindInvoiceData(View view) {

        TextView txtInvoiceId = view.findViewById(R.id.txtInvoiceId);
        TextView txtTherapist = view.findViewById(R.id.txtTherapist);
        TextView txtPatient = view.findViewById(R.id.txtPatient);
        TextView txtSessionDate = view.findViewById(R.id.txtSessionDate);
        TextView txtAmount = view.findViewById(R.id.txtAmount);
        TextView txtStatus = view.findViewById(R.id.txtStatus);

        String formattedDate = "N/A";
        if (invoice.getSessionDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            formattedDate = sdf.format(invoice.getSessionDate().toDate());
        }

        txtInvoiceId.setText("Invoice ID: " + invoice.getInvoiceId());
        txtTherapist.setText("Therapist: " +
                (TextUtils.isEmpty(invoice.getTherapistName()) ? "N/A" : invoice.getTherapistName()));
        txtPatient.setText("Patient: " +
                (TextUtils.isEmpty(invoice.getPatientName()) ? "N/A" : invoice.getPatientName()));
        txtSessionDate.setText("Session Date: " + formattedDate);
        txtAmount.setText("Amount: Rs. " + invoice.getAmount());
        txtStatus.setText("Status: " +
                (TextUtils.isEmpty(invoice.getStatus()) ? "N/A" : invoice.getStatus()));

        view.findViewById(R.id.btnSaveInvoice).setOnClickListener(v -> saveInvoiceAsPdf());
        view.findViewById(R.id.btnContactNow).setOnClickListener(v -> contactSupport());
    }

    private void saveInvoiceAsPdf() {
        try {
            PdfDocument pdfDocument = new PdfDocument();

            Paint titlePaint = new Paint();
            Paint textPaint = new Paint();
            Paint linePaint = new Paint();

            titlePaint.setTextSize(18f);
            titlePaint.setFakeBoldText(true);
            textPaint.setTextSize(12f);
            linePaint.setStrokeWidth(2f);

            PdfDocument.PageInfo pageInfo =
                    new PdfDocument.PageInfo.Builder(400, 700, 1).create();

            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            int x = 20;
            int y = 30;

            Drawable logo = ContextCompat.getDrawable(requireContext(), R.drawable.app_logo);
            if (logo != null) {
                Bitmap bitmap = Bitmap.createBitmap(
                        logo.getIntrinsicWidth(),
                        logo.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888
                );
                Canvas logoCanvas = new Canvas(bitmap);
                logo.setBounds(0, 0, logoCanvas.getWidth(), logoCanvas.getHeight());
                logo.draw(logoCanvas);

                Bitmap scaledLogo = Bitmap.createScaledBitmap(bitmap, 60, 60, false);
                canvas.drawBitmap(scaledLogo, x, y, null);
            }

            canvas.drawText("Speech Bridge", x + 80, y + 25, titlePaint);
            textPaint.setTextSize(10f);
            canvas.drawText("Codefline Pvt Limited", x + 80, y + 45, textPaint);

            y += 80;
            canvas.drawLine(x, y, 380, y, linePaint);
            y += 25;

            titlePaint.setTextSize(16f);
            canvas.drawText("INVOICE", x, y, titlePaint);
            y += 25;

            String formattedDate = "N/A";
            if (invoice.getSessionDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                formattedDate = sdf.format(invoice.getSessionDate().toDate());
            }

            canvas.drawText("Invoice ID: " + invoice.getInvoiceId(), x, y, textPaint); y += 20;
            canvas.drawText("Therapist: " + invoice.getTherapistName(), x, y, textPaint); y += 20;
            canvas.drawText("Patient: " + invoice.getPatientName(), x, y, textPaint); y += 20;
            canvas.drawText("Session Date: " + formattedDate, x, y, textPaint); y += 25;

            canvas.drawLine(x, y, 380, y, linePaint); y += 25;

            titlePaint.setTextSize(14f);
            canvas.drawText("Amount: Rs. " + invoice.getAmount(), x, y, titlePaint); y += 25;
            canvas.drawText("Status: " + invoice.getStatus(), x, y, textPaint); y += 40;

            textPaint.setTextSize(10f);
            canvas.drawText("Thank you for using Speech Bridge!", x, y, textPaint);

            pdfDocument.finishPage(page);

            String fileName = "invoice_" + invoice.getInvoiceId() + ".pdf";

            FileOutputStream fos = requireContext().openFileOutput(fileName, Context.MODE_PRIVATE);
            pdfDocument.writeTo(fos);
            fos.close();

            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            ContentResolver resolver = requireContext().getContentResolver();
            Uri uri = resolver.insert(MediaStore.Files.getContentUri("external"), values);

            if (uri != null) {
                OutputStream out = resolver.openOutputStream(uri);
                pdfDocument.writeTo(out);
                if (out != null) out.close();
            }

            File file = new File(requireContext().getFilesDir(), fileName);

            Uri fileUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
                    file
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);

            NotificationManager manager =
                    (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);

            String channelId = "invoice_channel";

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        channelId,
                        "Invoice Notifications",
                        NotificationManager.IMPORTANCE_HIGH
                );
                manager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), channelId)
                    .setSmallIcon(R.drawable.app_logo)
                    .setContentTitle("Invoice Saved")
                    .setContentText("Your invoice has been saved successfully!")
                    .setAutoCancel(true);

            manager.notify(1001, builder.build());

            pdfDocument.close();

            Toast.makeText(getContext(), "Invoice PDF created!", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void contactSupport() {
        String phoneNumber = "0714344721";

        try {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phoneNumber));

            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED) {
                startActivity(intent);
            } else {
                requestPermissions(new String[]{android.Manifest.permission.CALL_PHONE}, 101);
            }

        } catch (Exception e) {
            Toast.makeText(getContext(), "Unable to make a call", Toast.LENGTH_SHORT).show();
        }
    }
}