package chat;

import static javax.measure.unit.SI.KILOGRAM;

import java.util.Map;

import javax.measure.quantity.Mass;
import org.jscience.physics.model.RelativisticModel;
import org.springframework.web.bind.annotation.RequestMapping;
import org.jscience.physics.amount.Amount;

public class Chat
{
	
	public static void main(String args[])
	{
		ChatClientApp app=new ChatClientApp();
		app.setVisible(true);
	}
	
	@RequestMapping("/hello")
	String hello(Map<String, Object> model) {
	    RelativisticModel.select();
	    Amount<Mass> m = Amount.valueOf("12 GeV").to(KILOGRAM);
	    model.put("science", "E=mc^2: 12 GeV = " + m.toString());
	    return "hello";
	}
}
