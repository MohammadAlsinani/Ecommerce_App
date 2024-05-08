package com.app.partyshopping;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class AdminHomeFragment extends Fragment {

    private static final int REQUEST_CODE_IMAGE = 101;
    private static final int IMAGE_COUNT = 4;
    private static final int PICK_IMAGE_REQUEST = 1;


    private ImageView[] imageViews = new ImageView[IMAGE_COUNT];
    private Uri[] imageUris = new Uri[IMAGE_COUNT];
    private int currentImageIndex = 0;

    private TextInputLayout titleLayout, descriptionLayout, priceLayout;
    private TextInputEditText titleEditText, descriptionEditText, priceEditText;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private Spinner categorySpinner;
    private String selectedCategory;

    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);

        // Initialize Firebase references
        databaseReference = FirebaseDatabase.getInstance().getReference("Items");
        storageReference = FirebaseStorage.getInstance().getReference("item_images");

        // Initialize views
        AppCompatButton uploadButton = view.findViewById(R.id.upload);
        uploadButton.setOnClickListener(v -> uploadData());

        titleLayout = view.findViewById(R.id.title);
        descriptionLayout = view.findViewById(R.id.description);
        priceLayout = view.findViewById(R.id.price);

        titleEditText = Objects.requireNonNull((TextInputEditText) titleLayout.getEditText());
        descriptionEditText = Objects.requireNonNull((TextInputEditText) descriptionLayout.getEditText());
        priceEditText = Objects.requireNonNull((TextInputEditText) priceLayout.getEditText());

        categorySpinner = view.findViewById(R.id.category_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.categories_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategory = null;
            }
        });

        imageViews[0] = view.findViewById(R.id.image1);
        imageViews[1] = view.findViewById(R.id.image2);
        imageViews[2] = view.findViewById(R.id.image3);
        imageViews[3] = view.findViewById(R.id.image4);

        for (ImageView imageView : imageViews) {
            imageView.setOnClickListener(v -> pickImage());
        }

        progressBar = view.findViewById(R.id.progress_bar);

        return view;
    }

    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == -1 && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                imageViews[currentImageIndex].setImageURI(imageUri);
                imageUris[currentImageIndex] = imageUri;
                currentImageIndex++;
                if (currentImageIndex >= IMAGE_COUNT) {
                    uploadData();
                }
            }
        }
    }


    private void uploadData() {
        String title = Objects.requireNonNull(titleEditText.getText()).toString().trim();
        String description = Objects.requireNonNull(descriptionEditText.getText()).toString().trim();
        String price = Objects.requireNonNull(priceEditText.getText()).toString().trim();

        if (title.isEmpty() || description.isEmpty() || price.isEmpty() || containsNull(imageUris) || selectedCategory == null) {
            Toast.makeText(requireContext(), "Fill all fields and select images and category", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);

        String itemId = UUID.randomUUID().toString();
        List<String> imageUrls = new ArrayList<>();
        AtomicInteger uploadedCount = new AtomicInteger(); // Counter to track uploaded images

        for (int i = 0; i < IMAGE_COUNT; i++) {
            Uri imageUri = imageUris[i];
            if (imageUri != null) {
                StorageReference imageRef = storageReference.child(selectedCategory).child(itemId).child("image_" + (i + 1));
                UploadTask uploadTask = imageRef.putFile(imageUri);
                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        imageUrls.add(uri.toString());
                        uploadedCount.getAndIncrement();
                        if (uploadedCount.get() == IMAGE_COUNT) {
                            uploadItemWithImages(selectedCategory, itemId, title, description, price, imageUrls);
                        }
                    });
                }).addOnFailureListener(e -> {
                    // Hide progress bar on failure
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    private void uploadItemWithImages(String category, String itemId, String title, String description, String price, List<String> imageUrls) {
        Item item = new Item(itemId, title, description, price, imageUrls);

        databaseReference.child(category).child(itemId).setValue(item)
                .addOnSuccessListener(aVoid -> {
                    // Hide progress bar on success
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Item uploaded successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> {
                    // Hide progress bar on failure
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Failed to upload item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean containsNull(Uri[] array) {
        for (Uri uri : array) {
            if (uri == null) {
                return true;
            }
        }
        return false;
    }

    private void clearFields() {
        titleEditText.setText("");
        descriptionEditText.setText("");
        priceEditText.setText("");
        for (ImageView imageView : imageViews) {
            imageView.setImageResource(R.drawable.your_image); // Replace with default image
        }
        Arrays.fill(imageUris, null);
        currentImageIndex = 0;
    }
}
