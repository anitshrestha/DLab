/***************************************************************************

Copyright (c) 2016, EPAM SYSTEMS INC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

****************************************************************************/

import { Pipe, PipeTransform } from '@angular/core';

@Pipe({name: 'lineBreak'})
export class LineBreakPipe implements PipeTransform {
  transform(value: any, args?: any): any {
    if (value)
      if(value.indexOf('Master') > -1) {
        return value.replace(/\n/g, '<br/>');
      } else if(value.match(/\d x \S+/)) {
        return value.match(/\d x \S+/)[0].split(' x ')[1];
      }
    return value;
  }
}
