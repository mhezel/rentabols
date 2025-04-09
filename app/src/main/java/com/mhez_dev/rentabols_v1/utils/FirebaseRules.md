# Firebase Storage Rules Guide

To fix the image upload issue, you'll need to update your Firebase Storage security rules to allow authenticated users to upload files. Follow these steps:

## Configure Firebase Storage Rules

1. Go to the Firebase Console (https://console.firebase.google.com/)
2. Select your project
3. Navigate to "Storage" in the left sidebar
4. Click on the "Rules" tab
5. Update the rules to allow authenticated users to upload files:

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Allow read access to all users
    match /{allPaths=**} {
      allow read;
    }
    
    // Allow write access to authenticated users for their own user folder
    match /users/{userId}/{allPaths=**} {
      allow write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

These rules will:
1. Allow anyone to read images (public access to view images)
2. Only allow authenticated users to upload to their own user folder

## How to Apply These Rules

1. Copy the rules above
2. Paste them into the Firebase Storage Rules editor
3. Click "Publish" to deploy the rules

After updating the rules, the app should be able to upload images to Firebase Storage.

## Current Implementation

In our current implementation:
- Images are stored in `/users/{userId}/items/{itemId}/image_0.jpg`
- This ensures users can only upload to their own directory
- The rules above will allow this structure to work

Let me know if you need any further adjustments to the rules or implementation. 