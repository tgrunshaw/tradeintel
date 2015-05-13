/*
 * Copyright 2013 Kevin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nz.co.kevindoran.highcharts;

import java.io.IOException;
import java.util.List;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 *
 * @author Kevin
 */
@FacesComponent("UIColumnSeriesData")
public class UIColumnSeriesData extends UIComponentBase {
    
    private ColumnSeries columnSeries;
    @Override
    public String getFamily() {
        return "im.not.sure.what.goes.here";
    }
    
    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        ResponseWriter wr = context.getResponseWriter();
        wr.write("[");
        ColumnSeries s = (ColumnSeries) getAttributes().get("series");
        List<ColumnData> data = s.getColumns(); //= columnSeries.getColumns();
        int noOfColumns = data.size();
        if(noOfColumns > 0) {
            wr.append("" + data.get(0).getValue());
            for(int i=1; i<noOfColumns; i++) {
                wr.append("," + data.get(i).getValue());
            }
        }
        wr.append("]");
        super.encodeEnd(context);
    }
    
    public void setSeries(ColumnSeries s) {
        this.columnSeries = s;
    }
    
    public ColumnSeries getSeries(){
        return columnSeries;
    }
}
