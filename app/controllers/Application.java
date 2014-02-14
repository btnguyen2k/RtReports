package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import play.libs.F.Function0;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import vng.ubase.rtstats.Global;
import vng.ubase.rtstats.counter.CounterBlock;
import vng.ubase.rtstats.counter.ICounter;

public class Application extends Controller {

	public static Result index(String product, String position)
			throws Exception {
		String[] productList = Global.getProductNames();
		if (StringUtils.isEmpty(product)) {
			product = productList != null && productList.length > 0 ? productList[0]
					: "";
		}
		String[] positionList = Global.getCounterNamesForProduct(product);
		if (positionList == null) {
			positionList = ArrayUtils.EMPTY_STRING_ARRAY;
		}
		return ok(views.html.index.render(product, position, productList,
				positionList));
	}

	private static List<String> buildCounterKeys(String product, String position) {
		List<String> result = new ArrayList<String>();
		if (null == position || "-".equals(position) || "_".equals(position)) {
			// total value
			result.add(product);
		} else if ("*".equals(position)) {
			// all positions
			for (String pos : Global.getCounterNamesForProduct(product)) {
				result.add(product + "_" + pos);
			}
		} else {
			// single position
			result.add(product + "_" + position);
		}
		return result;
	}

	public static Promise<Result> ajaxGetData(final String _product,
			final String _position, final long _resolution) {
		Promise<Result> promise = Promise.promise(new Function0<Result>() {
			public Result apply() throws Exception {
				String product = _product;
				String position = _position;
				String[] productList = Global.getProductNames();
				if (StringUtils.isEmpty(product)) {
					product = productList != null && productList.length > 0 ? productList[0]
							: "";
				}
				String[] positionList = Global
						.getCounterNamesForProduct(product);
				if (positionList == null) {
					positionList = ArrayUtils.EMPTY_STRING_ARRAY;
				}

				List<Object> result = new ArrayList<Object>();

				List<String> keys = buildCounterKeys(product, position);
				for (String key : keys) {
					String label = key;
					List<Object[]> data = new ArrayList<Object[]>();
					Map<String, Object> plotData = new HashMap<String, Object>();
					result.add(plotData);
					plotData.put("label", label);
					plotData.put("data", data);
					ICounter counter = Global.counterFactory.getCounter(key);
					if (counter != null) {
						int NUM_BLOCKS = 100;
						CounterBlock[] blocks = counter.get(NUM_BLOCKS);
						int index = 0;
						for (CounterBlock block : blocks) {
							if (index == NUM_BLOCKS - 2) {
								break;
							}
							index++;
							Object[] entry = new Object[] { block.getKey(),
									block.getValue() };
							data.add(entry);
						}
					}
				}
				return ok(Json.toJson(result));
			}
		});
		return promise;
	}
}
