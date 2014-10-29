package org.wso2.carbon.mediator.publishevent.builders;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.mediator.publishevent.PublishEventMediatorException;
import org.wso2.carbon.mediator.publishevent.util.Constants;

public class CorrelationDataBuilder {

    private static final Log log = LogFactory.getLog(CorrelationDataBuilder.class);

    public Object[] createCorrelationData(MessageContext messageContext) throws PublishEventMediatorException {
        Object[] correlationData = new Object[Constants.NUM_OF_CONST_CORRELATION_PARAMS];
        int i= 0;
        try{
            correlationData[i] = messageContext.getProperty(Constants.MSG_BAM_ACTIVITY_ID);
            return correlationData;
        } catch (Exception e) {
            String errorMsg = "Error occurred while producing values for Correlation Data. " + e.getMessage();
            log.error(errorMsg, e);
            throw new PublishEventMediatorException(errorMsg, e);
        }
    }
}
