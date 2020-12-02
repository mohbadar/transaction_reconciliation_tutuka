import moment from 'moment';
import { Observable } from 'rxjs';
import { Component, OnInit, ElementRef, ViewChild, Injectable } from '@angular/core';
import { Location } from '@angular/common';
import { Validators, FormGroup, FormBuilder } from '@angular/forms';
import { MessageType, LayoutUtilsService } from 'app/core/_base/crud';


import { MatDialog, MatPaginator, MatSort, MatTableDataSource } from '@angular/material';
import { NgxSpinnerService } from 'ngx-spinner';
import { ReconciliationService } from './service/reconciliation.service';
import { ViewTransactionComponent } from './component/view-transaction/view-transaction.component';


@Component({
    selector: 'kt-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: ['dashboard.component.scss'],
})
export class DashboardComponent implements OnInit {

  hasFormErrors: boolean = false;

  myForm: FormGroup;

  clientCsv: File;
  clientCsvAdded: boolean;
  tutukaCsv: File;
  tutukaCsvAdded: boolean;

  resultLoaded: boolean = false;

  

  record: any = {};
  result: any;

  constructor(private formBuilder: FormBuilder,
      private location: Location,
      private layoutUtilService: LayoutUtilsService,
      private spinner: NgxSpinnerService,
      private reconciliationService: ReconciliationService
  ) { }


  ngOnInit() {
      this.myForm = this.formBuilder.group({
          clientCsv: [null,[Validators.required]],
          tutukaCsv: [null,[Validators.required]],
      });
  }




  submit() {
      const formData = new FormData();
      this.record = this.myForm.value;
      formData.append('info', JSON.stringify(this.record));
      formData.append('clientCsv', this.clientCsv);
      formData.append('tutukaCsv', this.tutukaCsv);

      console.log(formData);
      this.spinner.show();
      this.reconciliationService.processFiles(formData).subscribe((response) => {
          console.log("Response: ", response);
          this.resultLoaded = true;
          this.result = response;
          // this.myForm.reset();
          const msg = `Files are successfully processed!`;
          this.spinner.hide();
          this.layoutUtilService.showActionNotification(msg, MessageType.Create);
          // this.routeHome();
      }, (err) => {
          const msg = 'There was an error in processing files: '+ JSON.stringify(err);
          this.spinner.hide();
          this.layoutUtilService.showActionNotification(msg);
          console.log("err occured: ", err)
      });
  }

  routeHome() {
      this.location.back();
  }


  

  onClientCsvSelected(event) {
      if (event.target.files && event.target.files[0]) {
          const reader = new FileReader();
          this.clientCsv = event.target.files[0];
          console.log('FileName: ', event.target.files[0]);
          reader.readAsDataURL(event.target.files[0]); // read file as data url

          reader.onload = (e) => { // called once readAsDataURL is completed
              this.clientCsvAdded = true;
          }
      }
  }

  onTutukaCsvSelected(event) {
      if (event.target.files && event.target.files[0]) {
          const reader = new FileReader();
          this.tutukaCsv = event.target.files[0];
          console.log('FileName: ', event.target.files[0]);
          reader.readAsDataURL(event.target.files[0]); // read file as data url

          reader.onload = (e) => { // called once readAsDataURL is completed
              this.tutukaCsvAdded = true;
          }
      }
  }


}