/*
 * Copyright (c) {$year}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.mediator.publishevent.util;

/**
 * Convert Stream Properties to appropriate types.
 */
public class PropertyTypeConverter {

    public String convertToString(String string){
        if (string != null){
            return string;
        } else {
            return "";
        }
    }
    
    public int convertToInt(String string){
        try{
            return Integer.parseInt(string);
        } catch (NumberFormatException e){
            return 0;
        }
    }
    
    public float convertToFloat(String string){
        try{
            return Float.parseFloat(string);
        } catch (NumberFormatException e){
            return 0;
        }
    }
    
    public double convertToDouble(String string){
        try{
            return Double.parseDouble(string);
        } catch (NumberFormatException e){
            return 0;
        }
    }
    
    public long convertToLong(String string){
        try{
            return Long.parseLong(string);
        } catch (NumberFormatException e){
            return 0;
        }
    }
    
    public boolean convertToBoolean(String string){
        try{
            return Boolean.parseBoolean(string);
        } catch (Exception e){
            return false;
        }
    }
    
}
