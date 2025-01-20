package uk.ac.ed.inf;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonDeserializer;
import uk.ac.ed.inf.ilp.data.CreditCardInformation;
import uk.ac.ed.inf.ilp.data.Order;

import java.io.IOException;

public class CardDeserializer extends JsonDeserializer<CreditCardInformation> {

    @Override
    public CreditCardInformation deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String creditCardNumber = node.get("creditCardNumber").asText();
        String creditCardExpiry = node.get("creditCardExpiry").asText();
        String cvv = node.get("cvv").asText();

        return new CreditCardInformation(creditCardNumber,creditCardExpiry,cvv);
    }
}
