package us.dot.its.jpo.ode.services.asn1.message;

import org.apache.tomcat.util.buf.HexUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import us.dot.its.jpo.ode.OdeProperties;
import us.dot.its.jpo.ode.coder.StringPublisher;
import us.dot.its.jpo.ode.model.Asn1Encoding;
import us.dot.its.jpo.ode.model.Asn1Encoding.EncodingRule;
import us.dot.its.jpo.ode.model.OdeAsn1Data;
import us.dot.its.jpo.ode.model.OdeAsn1Payload;
import us.dot.its.jpo.ode.model.OdeSsmMetadata;

public class Asn1DecodeSSMJSON extends AbstractAsn1DecodeMessageJSON {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
	private ObjectMapper objectMapper = new ObjectMapper();

	public Asn1DecodeSSMJSON(OdeProperties odeProps) {
		super(new StringPublisher(odeProps), odeProps.getSsmStartFlag());
	}

	@Override
	protected Object process(String consumedData) {
        try {
			JSONObject rawSsmJsonObject = new JSONObject(consumedData);

			String jsonStringMetadata = rawSsmJsonObject.get("metadata").toString();
			OdeSsmMetadata metadata = objectMapper.readValue(jsonStringMetadata, OdeSsmMetadata.class);

			Asn1Encoding unsecuredDataEncoding = new Asn1Encoding("unsecuredData", "MessageFrame", EncodingRule.UPER);
			metadata.addEncoding(unsecuredDataEncoding);

			String payloadHexString = ((JSONObject)((JSONObject) rawSsmJsonObject.get("payload")).get("data")).getString("bytes");
			payloadHexString = super.stripDot2Header(payloadHexString);
			OdeAsn1Payload payload = new OdeAsn1Payload(HexUtils.fromHexString(payloadHexString));

			publishEncodedMessageToAsn1Decoder(new OdeAsn1Data(metadata, payload));
		} catch (Exception e) {
			logger.error("Error publishing to Asn1DecoderInput: {}", e.getMessage());
		}
		return null;
	}
}
