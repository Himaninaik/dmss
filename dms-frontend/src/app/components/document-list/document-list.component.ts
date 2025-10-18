// import { Component, OnInit } from '@angular/core';
// import { DocumentService, DocumentMetadata } from '../../services/document.service';
// import { saveAs } from 'file-saver';
// import { CommonModule, DatePipe } from '@angular/common';
// import { RouterModule } from '@angular/router';
// import { HttpClientModule } from '@angular/common/http';

// @Component({
//   selector: 'app-document-list',
//   templateUrl: './document-list.component.html',
//   //styleUrls: ['./document-list.component.css'], // can be empty for now
//   standalone: true,
//   imports: [CommonModule, RouterModule, DatePipe, HttpClientModule]
// })
// export class DocumentListComponent implements OnInit {

//   documents: DocumentMetadata[] = [];
//   loading: boolean = false;

//   constructor(private documentService: DocumentService) { }

//   ngOnInit(): void {
//     this.loadDocuments();
//   }

//   loadDocuments(): void {
//     this.loading = true;
//     this.documentService.list().subscribe({
//       next: (page) => {
//         this.documents = page.content; // assuming backend returns Page object
//         this.loading = false;
//       },
//       error: (err) => {
//         console.error(err);
//         this.loading = false;
//       }
//     });
//   }

//   deleteDocument(id: number): void {
//     if (!confirm('Are you sure you want to delete this document?')) return;

//     this.documentService.delete(id).subscribe({
//       next: () => this.loadDocuments(),
//       error: (err) => console.error(err)
//     });
//   }

//   downloadFile(document: DocumentMetadata): void {
//     if (!document.fileName) return;

//     const chunkSize = 1024 * 1024; // 1MB
//     const totalChunks = Math.ceil(document.fileSize! / chunkSize);
//     const fileId = document.fileName + '-' + document.id; // adjust if needed
//     let currentChunk = 0;
//     let chunks: Blob[] = [];

//     const downloadNextChunk = () => {
//       this.documentService.downloadChunk(fileId, currentChunk + 1).subscribe({
//         next: (chunk: Blob) => {
//           chunks.push(chunk);
//         },
//         complete: () => {
//           currentChunk++;
//           if (currentChunk < totalChunks) {
//             downloadNextChunk();
//           } else {
//             const blob = new Blob(chunks, { type: document.contentType || 'application/octet-stream' });
//             saveAs(blob, document.fileName);
//           }
//         },
//         error: (err) => console.error(err)
//       });
//     };

//     downloadNextChunk();
//   }
// }







// import { Component, OnInit } from '@angular/core';
// import { DocumentService, DocumentMetadata } from '../../services/document.service';
// import { saveAs } from 'file-saver';
// import { CommonModule, DatePipe } from '@angular/common';
// import { RouterModule } from '@angular/router';
// import { HttpClientModule } from '@angular/common/http';

// @Component({
//   selector: 'app-document-list',
//   templateUrl: './document-list.component.html',
//   standalone: true,
//   imports: [CommonModule, RouterModule, DatePipe, HttpClientModule]
// })
// export class DocumentListComponent implements OnInit {

//   documents: DocumentMetadata[] = [];
//   loading: boolean = false;

//   constructor(private documentService: DocumentService) { }

//   ngOnInit(): void { this.loadDocuments(); }

//   loadDocuments(): void {
//     this.loading = true;
//     this.documentService.list().subscribe({
//       next: (page) => { this.documents = page.content; this.loading = false; },
//       error: (err) => { console.error(err); this.loading = false; }
//     });
//   }

//   deleteDocument(id: number): void {
//     if (!confirm('Are you sure?')) return;
//     this.documentService.delete(id).subscribe({ next: () => this.loadDocuments(), error: (err) => console.error(err) });
//   }

//   downloadFile(document: DocumentMetadata): void {
//     if (!document.fileName) return;
//     const chunkSize = 1024 * 1024;
//     const totalChunks = Math.ceil(document.fileSize! / chunkSize);
//     let currentChunk = 0;
//     let chunks: Blob[] = [];

//     const downloadNextChunk = () => {
//       const start = currentChunk * chunkSize;
//       const end = Math.min(start + chunkSize, document.fileSize! - 1);

//       this.documentService.downloadChunk(document.storagePath!, start, end).subscribe({
//         next: (chunk: Blob) => chunks.push(chunk),
//         complete: () => {
//           currentChunk++;
//           if (currentChunk < totalChunks) downloadNextChunk();
//           else {
//             const blob = new Blob(chunks, { type: document.contentType || 'application/octet-stream' });
//             saveAs(blob, document.fileName);
//           }
//         },
//         error: (err) => console.error(err)
//       });
//     };

//     downloadNextChunk();
//   }
// }


import { Component, OnInit } from '@angular/core';
import { DocumentService, DocumentMetadata } from '../../services/document.service';
import { saveAs } from 'file-saver';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-document-list',
  templateUrl: './document-list.component.html',
  standalone: true,
  imports: [CommonModule, RouterModule, DatePipe, HttpClientModule]
})
export class DocumentListComponent implements OnInit {

  documents: DocumentMetadata[] = [];
  loading = false;

  constructor(private documentService: DocumentService) {}

  ngOnInit(): void {
    this.loadDocuments();
  }

  loadDocuments(): void {
    this.loading = true;
    this.documentService.list().subscribe({
      next: (page: any) => {
        this.documents = page.content || [];
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Error loading documents:', err);
        this.loading = false;
      }
    });
  }

  deleteDocument(id: number): void {
    if (!confirm('Are you sure you want to delete this document?')) return;
    this.documentService.delete(id).subscribe({
      next: () => this.loadDocuments(),
      error: (err: any) => console.error('Error deleting document:', err)
    });
  }

  downloadFile(document: DocumentMetadata): void {
    if (!document.storagePath || !document.fileName) return;

    const chunkSize = 1024 * 1024; // 1MB
    const totalChunks = Math.ceil(document.fileSize! / chunkSize);
    let currentChunk = 0;
    const chunks: Blob[] = [];

    const downloadNextChunk = () => {
      const start = currentChunk * chunkSize;
      const end = Math.min(start + chunkSize - 1, document.fileSize! - 1);

      this.documentService.downloadChunk(document.storagePath!, start, end).subscribe({
        next: (chunk: Blob) => chunks.push(chunk),
        complete: () => {
          currentChunk++;
          if (currentChunk < totalChunks) downloadNextChunk();
          else {
            const blob = new Blob(chunks, { type: document.contentType || 'application/octet-stream' });
            saveAs(blob, document.fileName);
          }
        },
        error: (err: any) => console.error('Error downloading chunk:', err)
      });
    };

    downloadNextChunk();
  }
}
