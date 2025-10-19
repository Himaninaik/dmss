// import { Component, OnInit } from '@angular/core';
// import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
// import { ActivatedRoute, Router, RouterModule } from '@angular/router';
// import { DocumentService, DocumentMetadata } from '../../services/document.service';
// import { HttpClientModule, HttpEventType } from '@angular/common/http';
// import { CommonModule } from '@angular/common';

// @Component({
//   selector: 'app-document-form',
//   templateUrl: './document-form.component.html',
//   //styleUrls: ['./document-form.component.css'], // keep empty file if needed
//   standalone: true,
//   imports: [CommonModule, ReactiveFormsModule, RouterModule, HttpClientModule]
// })
// export class DocumentFormComponent implements OnInit {

//   documentForm!: FormGroup;
//   documentId?: number;
//   isEditMode: boolean = false;
//   loading: boolean = false;

//   // For file upload
//   selectedFile?: File;
//   uploadProgress: number = 0;

//   constructor(
//     private fb: FormBuilder,
//     private documentService: DocumentService,
//     private router: Router,
//     private route: ActivatedRoute
//   ) { }

//   ngOnInit(): void {
//     this.documentId = this.route.snapshot.params['id'];
//     this.isEditMode = !!this.documentId;

//     this.documentForm = this.fb.group({
//       title: ['', Validators.required],
//       description: [''],
//       fileName: ['', Validators.required],
//       fileSize: [0, [Validators.required, Validators.min(0)]],
//       uploadedBy: ['', Validators.required]
//     });

//     if (this.isEditMode) {
//       this.loadDocument();
//     }
//   }

//   loadDocument(): void {
//     if (!this.documentId) return;

//     this.loading = true;
//     this.documentService.get(this.documentId).subscribe({
//       next: (doc: DocumentMetadata) => {
//         this.documentForm.patchValue({
//           title: doc.title,
//           description: doc.description,
//           fileName: doc.fileName,
//           fileSize: doc.fileSize,
//           uploadedBy: doc.uploadedBy
//         });
//         this.loading = false;
//       },
//       error: (err) => {
//         console.error(err);
//         this.loading = false;
//       }
//     });
//   }

//   onFileSelected(event: any): void {
//     const file: File = event.target.files[0];
//     if (file) {
//       this.selectedFile = file;
//       this.documentForm.patchValue({ fileName: file.name, fileSize: file.size });
//     }
//   }

//   uploadFile(metadataId: number): void {
//     if (!this.selectedFile) return;

//     const chunkSize = 1024 * 1024; // 1MB
//     const totalChunks = Math.ceil(this.selectedFile.size / chunkSize);
//     const fileId = this.selectedFile.name + '-' + Date.now();

//     let chunkNumber = 0;

//     const uploadNextChunk = () => {
//       const start = chunkNumber * chunkSize;
//       const end = Math.min(start + chunkSize, this.selectedFile!.size);
//       const chunk = this.selectedFile!.slice(start, end);

//       this.documentService.uploadChunk(fileId, chunk, chunkNumber + 1, totalChunks)
//         .subscribe({
//           next: (event) => {
//             if (event.type === HttpEventType.UploadProgress && event.total) {
//               this.uploadProgress = Math.round(((chunkNumber * chunkSize + event.loaded) / this.selectedFile!.size) * 100);
//             }
//           },
//           complete: () => {
//             chunkNumber++;
//             if (chunkNumber < totalChunks) {
//               uploadNextChunk();
//             } else {
//               this.documentService.finalizeUpload(fileId, metadataId).subscribe(() => {
//                 alert('File upload complete!');
//                 this.uploadProgress = 0;
//                 this.router.navigate(['/documents']);
//               });
//             }
//           },
//           error: (err) => console.error(err)
//         });
//     };

//     uploadNextChunk();
//   }

//   onSubmit(): void {
//     if (this.documentForm.invalid) return;

//     this.loading = true;
//     const formData: DocumentMetadata = this.documentForm.value;

//     const saveMetadata = this.isEditMode && this.documentId
//       ? this.documentService.update(this.documentId, formData)
//       : this.documentService.create(formData);

//     saveMetadata.subscribe({
//       next: (metadata) => {
//         if (this.selectedFile) {
//           this.uploadFile(metadata.id!);  // start chunked upload
//         } else {
//           this.router.navigate(['/documents']);
//         }
//       },
//       error: (err) => {
//         console.error(err);
//         this.loading = false;
//       }
//     });
//   }
// }


// src/app/components/document-form/document-form.component.ts
// import { Component, OnInit } from '@angular/core';
// import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
// import { ActivatedRoute, Router, RouterModule } from '@angular/router';
// import { DocumentService, DocumentMetadata } from '../../services/document.service';
// import { HttpClientModule, HttpEventType } from '@angular/common/http';
// import { CommonModule } from '@angular/common';

// @Component({
//   selector: 'app-document-form',
//   templateUrl: './document-form.component.html',
//   standalone: true,
//   imports: [CommonModule, ReactiveFormsModule, RouterModule, HttpClientModule]
// })
// export class DocumentFormComponent implements OnInit {

//   documentForm!: FormGroup;
//   documentId?: number;
//   isEditMode = false;
//   loading = false;

//   // File upload state
//   selectedFile?: File;
//   uploadProgress = 0;

//   constructor(
//     private fb: FormBuilder,
//     private documentService: DocumentService,
//     private router: Router,
//     private route: ActivatedRoute
//   ) {}

//   ngOnInit(): void {
//     this.documentId = this.route.snapshot.params['id'];
//     this.isEditMode = !!this.documentId;

//     this.documentForm = this.fb.group({
//       title: ['', Validators.required],
//       description: [''],
//       uploadedBy: ['', Validators.required],
//       fileName: ['', Validators.required],
//       fileSize: [0, [Validators.required, Validators.min(1)]]
//     });

//     if (this.isEditMode) {
//       this.loadDocument();
//     }
//   }

//   loadDocument(): void {
//     if (!this.documentId) return;

//     this.loading = true;
//     this.documentService.get(this.documentId).subscribe({
//       next: (doc: DocumentMetadata) => {
//         this.documentForm.patchValue({
//           title: doc.title,
//           description: doc.description,
//           uploadedBy: doc.uploadedBy,
//           fileName: doc.fileName,
//           fileSize: doc.fileSize
//         });
//         this.loading = false;
//       },
//       error: (err) => {
//         console.error('Error loading document:', err);
//         this.loading = false;
//       }
//     });
//   }

//   onFileSelected(event: any): void {
//     const file: File = event.target.files[0];
//     if (file) {
//       this.selectedFile = file;
//       this.documentForm.patchValue({
//         fileName: file.name,
//         fileSize: file.size
//       });
//     }
//   }

//   uploadFile(metadataId: number): void {
//     if (!this.selectedFile) return;

//     const chunkSize = 1024 * 1024; // 1MB
//     const totalChunks = Math.ceil(this.selectedFile.size / chunkSize);
//     const chunkId = this.selectedFile.name + '-' + Date.now();
//     let chunkNumber = 0;

//     const uploadNextChunk = () => {
//       const start = chunkNumber * chunkSize;
//       const end = Math.min(start + chunkSize, this.selectedFile!.size);
//       const chunk = this.selectedFile!.slice(start, end);

//       this.documentService.uploadChunk(chunkId, chunk, chunkNumber + 1, totalChunks, this.selectedFile!.name)
//         .subscribe({
//           next: (event) => {
//             if (event.type === HttpEventType.UploadProgress && event.total) {
//               this.uploadProgress = Math.round(
//                 ((chunkNumber * chunkSize + event.loaded) / this.selectedFile!.size) * 100
//               );
//             }
//           },
//           complete: () => {
//             chunkNumber++;
//             if (chunkNumber < totalChunks) {
//               uploadNextChunk();
//             } else {
//               this.documentService.finalizeUpload(chunkId, metadataId).subscribe({
//                 next: () => {
//                   alert('File upload complete!');
//                   this.uploadProgress = 0;
//                   this.router.navigate(['/documents']);
//                 },
//                 error: (err) => console.error('Finalize upload error:', err)
//               });
//             }
//           },
//           error: (err) => console.error('Chunk upload error:', err)
//         });
//     };

//     uploadNextChunk();
//   }

//   onSubmit(): void {
//     if (this.documentForm.invalid) {
//       alert('Please fill all required fields.');
//       return;
//     }

//     this.loading = true;

//     // Prepare metadata with proper file info
//     const formData: DocumentMetadata = {
//       ...this.documentForm.value,
//       fileName: this.selectedFile?.name || this.documentForm.value.fileName,
//       fileSize: this.selectedFile?.size || this.documentForm.value.fileSize
//     };

// //     const save$ = this.isEditMode && this.documentId
// //       ? this.documentService.update(this.documentId, formData)
// //       : this.documentService.create(formData);

// //     save$.subscribe({
// //       next: (metadata) => {
// //         if (this.selectedFile) {
// //           this.uploadFile(metadata.id!); // upload new file if selected
// //         } else {
// //           this.router.navigate(['/documents']); // navigate if only metadata updated
// //         }
// //       },
// //       error: (err) => {
// //         console.error('Error saving metadata:', err);
// //         alert('Failed to save document. Ensure all required fields are filled correctly.');
// //         this.loading = false;
// //       }
// //     });
// //   }
// // }
// if (this.isEditMode && this.documentId) {
//       // Edit mode: update metadata only
//       this.documentService.update(this.documentId, metadata).subscribe({
//         next: () => {
//           if (this.selectedFile) {
//             this.uploadFile(this.documentId!);
//           } else {
//             alert('Document updated successfully!');
//             this.router.navigate(['/documents']);
//           }
//         },
//         error: (err) => {
//           console.error('Error updating metadata:', err);
//           alert('Failed to update metadata.');
//           this.loading = false;
//         }
//       });
//     } else {
//       // Create mode: upload file with metadata
//       const formData = new FormData();
//       if (this.selectedFile) formData.append('file', this.selectedFile);
//       formData.append('title', metadata.title);
//       formData.append('description', metadata.description || '');
//       formData.append('uploadedBy', metadata.uploadedBy);

//       this.documentService.uploadFile(formData).subscribe({
//         next: (res: DocumentMetadata) => {
//           alert('File uploaded successfully!');
//           this.router.navigate(['/documents']);
//         },
//         error: (err) => {
//           console.error('Error uploading file:', err);
//           alert('Failed to upload file.');
//           this.loading = false;
//         }
//       });
//     }
//   }
// }

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { DocumentService, DocumentMetadata } from '../../services/document.service';
import { HttpClientModule, HttpEventType } from '@angular/common/http';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-document-form',
  templateUrl: './document-form.component.html',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, HttpClientModule]
})
export class DocumentFormComponent implements OnInit {

  documentForm!: FormGroup;
  documentId?: number;
  isEditMode = false;
  loading = false;

  selectedFile?: File;
  uploadProgress = 0;

  constructor(
    private fb: FormBuilder,
    private documentService: DocumentService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.documentId = this.route.snapshot.params['id'];
    this.isEditMode = !!this.documentId;

    // Only show title (readonly) and file input
    this.documentForm = this.fb.group({
      title: [{ value: '', disabled: true }, Validators.required],
      fileName: ['', Validators.required],
      fileSize: [0, [Validators.required, Validators.min(1)]]
    });

    if (this.isEditMode) {
      this.loadDocument();
    }
  }

  loadDocument(): void {
    if (!this.documentId) return;

    this.loading = true;
    this.documentService.get(this.documentId).subscribe({
      next: (doc: DocumentMetadata) => {
        this.documentForm.patchValue({
          title: doc.title,
          fileName: doc.fileName,
          fileSize: doc.fileSize
        });
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Error loading document:', err);
        this.loading = false;
      }
    });
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      this.documentForm.patchValue({
        fileName: file.name,
        fileSize: file.size
      });
    }
  }

  onSubmit(): void {
    if (!this.selectedFile) {
      alert('Please select a file to update.');
      return;
    }

    if (!this.isEditMode || !this.documentId) {
      alert('Invalid operation. Only edit mode can update a file.');
      return;
    }

    this.loading = true;
    this.uploadProgress = 0;
    const chunkSize = 1024 * 1024; // 1MB
    const totalChunks = Math.ceil(this.selectedFile.size / chunkSize);
    const chunkId = this.selectedFile.name + '-' + Date.now();
    let chunkNumber = 0;

    const uploadNextChunk = () => {
      const start = chunkNumber * chunkSize;
      const end = Math.min(start + chunkSize, this.selectedFile!.size);
      const chunk = this.selectedFile!.slice(start, end);

      this.documentService.uploadChunk(chunkId, chunk, chunkNumber + 1, totalChunks, this.selectedFile!.name)
        .subscribe({
          next: (event) => {
            if (event.type === HttpEventType.UploadProgress && event.total) {
              this.uploadProgress = Math.round(
                ((chunkNumber * chunkSize + event.loaded) / this.selectedFile!.size) * 100
              );
            }
          },
          error: (err: any) => {
            console.error(`Error uploading chunk ${chunkNumber + 1}:`, err);
            alert(`Failed to upload chunk ${chunkNumber + 1}. Please try again.`);
            this.loading = false;
          },
          complete: () => {
            chunkNumber++;
            if (chunkNumber < totalChunks) {
              uploadNextChunk();
            } else {
              this.documentService.finalizeUpload(chunkId, this.documentId!).subscribe({
                next: () => {
                  alert('File updated successfully!');
                  this.router.navigate(['/documents']);
                },
                error: (err: any) => {
                  console.error('Finalize upload error:', err);
                  alert('Failed to finalize upload. Please try again.');
                  this.loading = false;
                },
                complete: () => {
                  this.loading = false;
                }
              });
            }
          }
        });
    };

    uploadNextChunk();
  }
}
