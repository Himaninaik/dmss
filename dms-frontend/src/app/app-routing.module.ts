import { NgModule } from '@angular/core';
import {  Routes,provideRouter } from '@angular/router';
import { DocumentListComponent } from './components/document-list/document-list.component';
import { DocumentFormComponent } from './components/document-form/document-form.component';

const routes: Routes = [
  { path: '', redirectTo: 'documents', pathMatch: 'full' },
  { path: 'documents', component: DocumentListComponent },
  { path: 'documents/add', component: DocumentFormComponent },
  { path: 'documents/edit/:id', component: DocumentFormComponent },
  { path: '**', redirectTo: 'documents' } // fallback
];

// @NgModule({
//   imports: [RouterModule.forRoot(routes)],
//   exports: [RouterModule]
// })
export const appRouterProviders =[ provideRouter(routes)];