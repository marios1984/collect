/**
 * Generated by Gas3 v2.3.0 (Granite Data Services).
 *
 * WARNING: DO NOT CHANGE THIS FILE. IT MAY BE OVERWRITTEN EACH TIME YOU USE
 * THE GENERATOR.
 */

package org.openforis.collect.remoting.service.dataimport {

    import org.granite.util.Enum;

    [Bindable]
    [RemoteClass(alias="org.openforis.collect.remoting.service.dataimport.DataImportState$MainStep")]
    public class DataImportState$MainStep extends Enum {

        public static const INITED:DataImportState$MainStep = new DataImportState$MainStep("INITED", _);
        public static const SUMMARY_CREATION:DataImportState$MainStep = new DataImportState$MainStep("SUMMARY_CREATION", _);
        public static const IMPORT:DataImportState$MainStep = new DataImportState$MainStep("IMPORT", _);

        function DataImportState$MainStep(value:String = null, restrictor:* = null) {
            super((value || INITED.name), restrictor);
        }

        protected override function getConstants():Array {
            return constants;
        }

        public static function get constants():Array {
            return [INITED, SUMMARY_CREATION, IMPORT];
        }

        public static function valueOf(name:String):DataImportState$MainStep {
            return DataImportState$MainStep(INITED.constantOf(name));
        }
    }
}