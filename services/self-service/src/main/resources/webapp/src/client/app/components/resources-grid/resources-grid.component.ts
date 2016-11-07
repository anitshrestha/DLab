/******************************************************************************************************

Copyright (c) 2016 EPAM Systems Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*****************************************************************************************************/

import { Component, Input, Output, ViewChild, OnInit } from "@angular/core";
import { UserResourceService } from "./../../services/userResource.service";
import { ResourcesGridRowModel } from './resources-grid.model';
import { CreateEmrModel } from "./createEmrModel";
import { ComputationalResourceImage } from "../../models/computationalResourceImage.model";
import { ConfirmationDialogType } from "../confirmation-dialog/confirmation-dialog-type.enum";

@Component({
  moduleId: module.id,
  selector: 'resources-grid',
  templateUrl: 'resources-grid.component.html',
  styleUrls: ['./resources-grid.component.css']
})

export class ResourcesGrid implements OnInit {

  isFilled: boolean = false;
  environments: Array<ResourcesGridRowModel>;
  notebookName: string;
  namePattern: string = "\\w+.*\\w+";
  model = new CreateEmrModel('', '');
  isOutscreenDropdown: boolean;

  @ViewChild('createEmrModal') createEmrModal;
  @ViewChild('confirmationDialog') confirmationDialog;
  @ViewChild('detailDialog') detailDialog;

  computationalResourcesImages: Array<ComputationalResourceImage> = [];

  constructor(
    private userResourceService: UserResourceService
  ) { }

  ngOnInit() : void {
    this.buildGrid();
    this.loadTemplates();

  }

  buildGrid() : void {
    this.userResourceService.getUserProvisionedResources()
      .subscribe((result) => {
        this.environments = this.loadEnvironments(result);

        console.log('models ', this.environments);
      });
  }

  loadTemplates() {
    this.userResourceService.getComputationalResourcesTemplates()
      .subscribe( data => {
        for(let parentIndex = 0; parentIndex < data.length; parentIndex ++)
          this.computationalResourcesImages.push(new ComputationalResourceImage(data[parentIndex]));
      }, error => this.computationalResourcesImages = []);
  }

  containsNotebook(notebook_name: string) : boolean {

    if(notebook_name)
      for (var index = 0; index < this.environments.length; index++)
        if(notebook_name.toLowerCase() ==  this.environments[index].name.toString().toLowerCase())
          return true;

        return false;
  }

  loadEnvironments(exploratoryList: Array<any>) : Array<ResourcesGridRowModel> {
     if (exploratoryList) {
       return exploratoryList.map((value) => {
         return new ResourcesGridRowModel(value.exploratory_name,
           value.status,
           value.shape,
           value.computational_resources,
           value.up_time_since,
           value.url,
           value.ip);
       });
     }
   }

  printDetailEnvironmentModal(data) : void {
    this.detailDialog.open({ isFooter: false }, data);
  }

  exploratoryAction(data, action:string) {
    console.log('action ' + action, data);
    if (action === 'deploy') {
      this.notebookName = data.name;
      this.createEmrModal.open({ isFooter: false });
    } else if (action === 'run') {
      this.userResourceService
        .runExploratoryEnvironment({notebook_instance_name: data.name})
        .subscribe((result) => {
          console.log('startUsernotebook result: ', result);
          this.buildGrid();
        });
    } else if (action === 'stop') {
      this.confirmationDialog.open({ isFooter: false }, data, ConfirmationDialogType.StopExploratory);
    } else if (action === 'terminate') {
      this.confirmationDialog.open({ isFooter: false }, data, ConfirmationDialogType.TerminateExploratory);
    }
  }

  createEmr(name, count, shape_master, shape_slave, version) {

    this.userResourceService
      .createComputationalResource({
        name: name,
        emr_instance_count: count,
        emr_master_instance_type: shape_master,
        emr_slave_instance_type: shape_slave,
        emr_version: version,
        notebook_name: this.notebookName
      })
      .subscribe((result) => {
        console.log('result: ', result);

        if (this.createEmrModal.isOpened) {
         this.createEmrModal.close();
       }
       this.buildGrid();
      });
      return false;
  };

  dropdownPosition(event) {
    let contentHeight = document.body.offsetHeight > window.outerHeight ? document.body.offsetHeight : window.outerHeight;
    this.isOutscreenDropdown = event.pageY + 215 > contentHeight ? true : false;
  }
}
