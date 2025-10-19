

// src/app/services/document.service.ts
// import { Injectable } from '@angular/core';
// import { HttpClient, HttpEvent, HttpRequest } from '@angular/common/http';
// import { Observable } from 'rxjs';

// export interface DocumentMetadata {
//   id?: number;
//   title: string;
//   description?: string;
//   fileName: string;
//   fileSize: number;
//   uploadedBy: string;
//   contentType?: string;
//   uploadDate?: string;
// }

// @Injectable({
//   providedIn: 'root'
// })
// export class DocumentService {

//   constructor(private http: HttpClient) { }

//   // -------------------- Metadata CRUD --------------------

//   list(page: number = 0, size: number = 10, sortBy: string = 'uploadDate', direction: string = 'DESC'): Observable<any> {
//     return this.http.get(`/api/metadata?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`);
//   }

//   get(id: number): Observable<DocumentMetadata> {
//     return this.http.get<DocumentMetadata>(`/api/metadata/${id}`);
//   }

//   create(metadata: DocumentMetadata): Observable<DocumentMetadata> {
//     return this.http.post<DocumentMetadata>('/api/metadata', metadata);
//   }

//   update(id: number, metadata: DocumentMetadata): Observable<DocumentMetadata> {
//     return this.http.put<DocumentMetadata>(`/api/metadata/${id}`, metadata);
//   }

//   delete(id: number): Observable<void> {
//     return this.http.delete<void>(`/api/metadata/${id}`);
//   }

//   // -------------------- File Upload --------------------

//   uploadChunk(fileId: string, chunk: Blob, chunkNumber: number, totalChunks: number): Observable<HttpEvent<any>> {
//     const formData = new FormData();
//     formData.append('chunk', chunk);
//     formData.append('chunkNumber', chunkNumber.toString());
//     formData.append('totalChunks', totalChunks.toString());

//     const req = new HttpRequest('POST', `/api/files/upload-chunk/${fileId}`, formData, {
//       reportProgress: true,
//     });

//     return this.http.request(req);
//   }

//   finalizeUpload(fileId: string, metadataId: number): Observable<any> {
//     return this.http.post(`/api/files/finalize-upload/${fileId}/${metadataId}`, {});
//   }

//   // -------------------- File Download --------------------

//   downloadChunk(fileId: string, chunkNumber: number): Observable<Blob> {
//     return this.http.get(`/api/files/download-chunk/${fileId}/${chunkNumber}`, { responseType: 'blob' });
//   }

// }


// src/app/services/document.service.ts
// src/app/services/document.service.ts
// 
// src/app/services/document.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpEvent, HttpRequest, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DocumentMetadata {
  id?: number;
  title: string;
  description?: string;
  fileName: string;
  fileSize: number;
  uploadedBy: string;
  contentType?: string;
  uploadDate?: string;
  storagePath?: string;
  version?: number;
}

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  // Use proxy configuration for development
  private readonly BASE_URL = '';

  constructor(private http: HttpClient) {}

  // -------------------- Metadata CRUD --------------------
  list(page = 0, size = 10, sortBy = 'uploadDate', direction = 'DESC'): Observable<any> {
    return this.http.get(`${this.BASE_URL}/api/metadata?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`);
  }

  get(id: number): Observable<DocumentMetadata> {
    return this.http.get<DocumentMetadata>(`${this.BASE_URL}/api/metadata/${id}`);
  }

  create(metadata: DocumentMetadata): Observable<DocumentMetadata> {
    return this.http.post<DocumentMetadata>(`${this.BASE_URL}/api/metadata`, metadata);
  }

  update(id: number, metadata: DocumentMetadata): Observable<DocumentMetadata> {
    return this.http.put<DocumentMetadata>(`${this.BASE_URL}/api/metadata/${id}`, metadata);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/api/metadata/${id}`);
  }

  // -------------------- File Upload --------------------
  uploadChunk(fileId: string, chunk: Blob, chunkNumber: number, totalChunks: number, fileName: string): Observable<HttpEvent<any>> {
    const formData = new FormData();
    formData.append('chunk', chunk);
    formData.append('chunkId', fileId);
    formData.append('chunkNumber', chunkNumber.toString());
    formData.append('totalChunks', totalChunks.toString());
    formData.append('fileName', fileName);

    const req = new HttpRequest('POST', `${this.BASE_URL}/api/files/upload-chunk`, formData, { reportProgress: true });
    return this.http.request(req);
  }

  finalizeUpload(chunkId: string, metadataId: number): Observable<any> {
    // Encode the chunkId to handle Unicode / special characters
    const safeChunkId = encodeURIComponent(chunkId);
    return this.http.post(`${this.BASE_URL}/api/files/finalize-upload/${safeChunkId}/${metadataId}`, {});
  }

  // -------------------- File Download --------------------
  downloadChunk(fileName: string, start: number, end: number): Observable<Blob> {
    const headers = new HttpHeaders({ 'Range': `bytes=${start}-${end}` });
    return this.http.get(`${this.BASE_URL}/api/files/download-chunk/${fileName}`, { headers, responseType: 'blob' });
  }
}
