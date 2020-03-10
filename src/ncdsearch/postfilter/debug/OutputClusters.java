package ncdsearch.postfilter.debug;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ncdsearch.postfilter.Clusters;
import ncdsearch.postfilter.JsonNodesInfo;

public class OutputClusters {
	private Clusters clusters;

	public OutputClusters(Clusters cs) {
		this.clusters = cs;
	}

	public void output() {
		try {
			BufferedWriter bw = Files.newBufferedWriter(Paths.get("F://ncdsearch/rereresult.txt"),
					StandardOpenOption.APPEND);
			bw.write("---------------------");
			bw.write("\n");
			for (List<JsonNode> list : clusters.getClusterReps()) {

				//			System.out.println("---------------------");
				//			System.out.println("rep:::" + list.get(0));
				//			clusters.getRepJsonMap().get(list.get(0)).forEach(s -> System.out.println(s));

				bw.write("---------------------");
				bw.write("\n");
				//bw.write("rep:::" + list.get(0).get("Tokens").asText());
				bw.write("rep:::" + list.get(0).toString());
				bw.write("\n");
				clusters.getRepJsonMap().get(list.get(0)).forEach(s -> {
					try {
						bw.write(s.toString());
						//bw.write(s.get("Tokens").asText());
						bw.write("\n");
					} catch (IOException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					}
				});
			}
			bw.write("//////////////////////////////////////////");
			bw.write("\n");
			bw.close();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}


	public void outputSorted() {
		try {
			BufferedWriter bw = Files.newBufferedWriter(Paths.get("F://ncdsearch/rereresult.txt"),
					StandardOpenOption.APPEND);
			bw.write("---------------------");
			bw.write("\n");
			for (List<JsonNode> list : clusters.getClusterContents()) {
				List<JsonNode> sortedlist= JsonNodesInfo.getSortedListbyDistance(list);
				//			System.out.println("---------------------");
				//			System.out.println("rep:::" + list.get(0));
				//			clusters.getRepJsonMap().get(list.get(0)).forEach(s -> System.out.println(s));

				bw.write("---------------------");
				bw.write("\n");
				//bw.write("rep:::" + list.get(0).get("Tokens").asText());

				sortedlist.forEach(s -> {
					try {
						bw.write(s.toString());
						//bw.write(s.get("Tokens").asText());
						bw.write("\n");
					} catch (IOException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					}
				});
			}
			bw.write("//////////////////////////////////////////");
			bw.write("\n");
			bw.close();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	public void outputJson(int ID, String dir) {

		//List<List<JsonNode>> jsonList = new ArrayList<>(clusters.getClusterReps());
		int index[] = new int[1];
		index[0] = 1;
		for (List<JsonNode> list : clusters.getClusterReps()) {
			clusters.getRepJsonMap().get(list.get(0)).forEach(node -> {
				((ObjectNode) node).put("clusterID", index[0]);
			});
			index[0]++;
		}

		ResultJson rj = new ResultJson(clusters.getAllNode());
		try {
			ObjectMapper mapper = new ObjectMapper();
			List<String> lines = new ArrayList<>();
			lines.add(mapper.writeValueAsString((Object) rj));
			Path path = Paths.get(dir, "clustering_result", "result-" + ID + ".json");
			if (Files.exists(path)) {
				Files.delete(path);
			}
			Files.createFile(path);
			Files.write(path, lines, Charset.forName("UTF-8"),
					StandardOpenOption.WRITE);

			//
		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		//			System.out.println(jsonList.toString());
		//			for (List<JsonNode> list : jsonList) {
		//				clusters.getRepJsonMap().get(list.get(0)).forEach(s -> {
		//					try {
		//						bw.write(s.toString());
		//						//bw.write(s.get("Tokens").asText());
		//						bw.write("\n");
		//					} catch (IOException e) {
		//						// TODO 自動生成された catch ブロック
		//						e.printStackTrace();
		//					}
		//				});
		//			}

	}

	public class ResultJson {
		List<JsonNode> Result;

		public ResultJson(List<JsonNode> Result) {
			this.Result = Result;
		}

		public List<JsonNode> getResult() {
			return Result;
		}
	}


}
