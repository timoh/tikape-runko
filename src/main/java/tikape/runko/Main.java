package tikape.runko;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.ModelAndView;
import static spark.Spark.*;
import spark.template.thymeleaf.ThymeleafTemplateEngine;
import tikape.runko.database.Database;
import tikape.runko.database.RaakaaineDao;
import tikape.runko.database.AnnosDao;
import tikape.runko.database.AnnosRaakaaineDao;
import tikape.runko.domain.Annos;
import tikape.runko.domain.AnnosRaakaaine;
import tikape.runko.domain.Raakaaine;

public class Main {

    public static void main(String[] args) throws Exception {
        Database database = new Database("jdbc:sqlite:smoothiearkisto.db");
        database.init();

        RaakaaineDao raakaaineDao = new RaakaaineDao(database);
        AnnosDao annosDao = new AnnosDao(database);
        AnnosRaakaaineDao annosRaakaaineDao = new AnnosRaakaaineDao(database);

        get("/", (req, res) -> {
            
            HashMap map = new HashMap<>();
            map.put("annos_index", annosDao.findAll());

            return new ModelAndView(map, "index");
        }, new ThymeleafTemplateEngine());

        get("/annos/", (req, res) -> {
            
            HashMap map = new HashMap<>();
            
            map.put("raakaaine_index", raakaaineDao.findAll());
            map.put("annos_index", annosDao.findAll());

            return new ModelAndView(map, "annos_index");
        }, new ThymeleafTemplateEngine());

        get("/annos/:id", (req, res) -> {
            HashMap map = new HashMap<>();
            int annosid = Integer.parseInt(req.params("id"));
            
            map.put("annos_show", annosDao.findOne(annosid));
            map.put("raaka_aineet", raakaaineDao.findAllWithAnnos(annosid));
            map.put("tyovaiheet", annosRaakaaineDao.findAllWithAnnos(annosid));

            return new ModelAndView(map, "annos_show");
        }, new ThymeleafTemplateEngine());
        
        post("/annos/", (req, res) -> {

            Annos annos = new Annos(null, req.queryParams("nimi"));
            
            
            try {
                annosDao.saveOrUpdate(annos);
                
            } catch (java.lang.RuntimeException e) {
                res.status(403);
                res.body("Virheellinen pyyntö! Lisätietoja:"+e.toString());
                res.redirect("/annos/");
                return ""; 
            }
            
            res.redirect("/annos/");
            return "";
            
        });
        
        post("/annosraakaaine/", (req, res) -> {
            
            AnnosRaakaaine ohje = new AnnosRaakaaine(null, req.queryParams("ohje"),
                Integer.parseInt(req.queryParams("jarjestys")), 
                Integer.parseInt(req.queryParams("smoothie")),
                Integer.parseInt(req.queryParams("raakaAine")), 
                req.queryParams("maara"));
            System.out.println("Smoothie: "+req.queryParams("smoothie"));
            System.out.println("Ohje: "+req.queryParams("ohje"));
            System.out.println("Määrä: "+req.queryParams("maara"));
            System.out.println("Raaka-aine: "+req.queryParams("raakaAine"));
            System.out.println("Järjestys: "+req.queryParams("jarjestys"));
            
            try {
                annosRaakaaineDao.saveOrUpdate(ohje);
                
            } catch (java.lang.RuntimeException e) {
                res.status(403);
                res.body("Virheellinen pyyntö! Lisätietoja:"+e.toString());
                res.redirect("/annos/");
                return ""; 
            }
            
            res.redirect("/annos/");
            return "";
            
        });
        
        post("/raakaaine/", (req, res) -> {

            Raakaaine aine = new Raakaaine(null, req.queryParams("nimi"));
            
            
            try {
                raakaaineDao.saveOrUpdate(aine);
                
            } catch (java.lang.RuntimeException e) {
                res.status(403);
                res.body("Virheellinen pyyntö! Lisätietoja:"+e.toString());
                res.redirect("/raakaaine/");
                return ""; 
            }
            
            res.redirect("/raakaaine/");
            return "";
            
        });
        
        post("/poista_raakaaine/", (req, res) -> {
            
            int raakaaine_id = Integer.parseInt(req.queryParams("raakaAine"));
            try {
                if (!annosRaakaaineDao.isRaakaaineUsed(raakaaine_id)) {
                    raakaaineDao.delete(raakaaine_id);
                }
            } catch (java.lang.RuntimeException e) {
                res.status(403);
                res.body("Virheellinen pyyntö! Lisätietoja:"+e.toString());
                res.redirect("/raakaaine/");
                return ""; 
            }
            
            res.redirect("/raakaaine/");
            return "";
        });
        
        get("/raakaaine/", (req, res) -> {
            HashMap map = new HashMap<>();
            map.put("raakaaine_index", raakaaineDao.findAll());

            return new ModelAndView(map, "raakaaine_index");
        }, new ThymeleafTemplateEngine());

        get("/raakaaine/:id", (req, res) -> {
            HashMap map = new HashMap<>();
            int id = Integer.parseInt(req.params("id"));
            map.put("raakaaine_show", raakaaineDao.findOne(id));
            map.put("annokset", annosDao.findAllWithRaakaaine(id));
            return new ModelAndView(map, "raakaaine_show");
        }, new ThymeleafTemplateEngine());        
    }
}
