/**
 * Generated by Gas3 v2.3.0 (Granite Data Services).
 *
 * WARNING: DO NOT CHANGE THIS FILE. IT MAY BE OVERWRITTEN EACH TIME YOU USE
 * THE GENERATOR.
 */

package org.openforis.collect.remoting.service.dataexport {

    import org.granite.util.Enum;

    [Bindable]
    [RemoteClass(alias="org.openforis.collect.remoting.service.dataexport.DataExportState$Format")]
    public class DataExportState$Format extends Enum {

        public static const XML:DataExportState$Format = new DataExportState$Format("XML", _);
        public static const CSV:DataExportState$Format = new DataExportState$Format("CSV", _);

        function DataExportState$Format(value:String = null, restrictor:* = null) {
            super((value || XML.name), restrictor);
        }

        protected override function getConstants():Array {
            return constants;
        }

        public static function get constants():Array {
            return [XML, CSV];
        }

        public static function valueOf(name:String):DataExportState$Format {
            return DataExportState$Format(XML.constantOf(name));
        }
    }
}