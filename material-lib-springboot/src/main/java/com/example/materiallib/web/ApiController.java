package com.example.materiallib.web;

import com.example.materiallib.model.*;
import com.example.materiallib.repo.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final MaterialRepository materials;
    private final ConditionRepository conditions;
    private final PropertyRepository properties;
    private final CurveRepository curves;
    private final CurvePointRepository curvePoints;

    public ApiController(MaterialRepository materials,
                         ConditionRepository conditions,
                         PropertyRepository properties,
                         CurveRepository curves,
                         CurvePointRepository curvePoints) {
        this.materials = materials;
        this.conditions = conditions;
        this.properties = properties;
        this.curves = curves;
        this.curvePoints = curvePoints;
    }

    // Examples:
    //   GET /api/materials?limit=50
    //   GET /api/materials?name=Aluminum&limit=50
    //   GET /api/materials?category=metal&limit=50
    @GetMapping("/materials")
    public List<Material> listMaterials(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "50") int limit
    ) {
        limit = Math.max(1, Math.min(limit, 500));
        if (name != null && !name.isBlank()) return materials.searchByName(name, limit);
        if (category != null && !category.isBlank()) return materials.byCategory(category, limit);
        return materials.findAll(limit);
    }

    @GetMapping("/materials/{id}")
    public Material material(@PathVariable long id) {
        return materials.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Material not found: " + id));
    }

    @GetMapping("/materials/{id}/conditions")
    public List<MaterialCondition> conditionsForMaterial(@PathVariable long id) {
        return conditions.findByMaterialId(id);
    }

    @GetMapping("/materials/{id}/default-condition")
    public MaterialCondition defaultCondition(@PathVariable long id) {
        return conditions.findDefaultForMaterial(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Default condition not found for material: " + id));
    }

    @GetMapping("/conditions/{id}/properties")
    public List<ConditionProperty> props(
            @PathVariable long id,
            @RequestParam(required = false) String key,
            @RequestParam(defaultValue = "200") int limit
    ) {
        limit = Math.max(1, Math.min(limit, 2000));
        if (key != null && !key.isBlank()) return properties.findByConditionIdAndKey(id, key, limit);
        return properties.findByConditionId(id, limit);
    }

    @GetMapping("/conditions/{id}/curves")
    public List<Curve> curvesForCondition(@PathVariable long id) {
        return curves.findByConditionId(id);
    }

    @GetMapping("/curves/{id}")
    public Curve curve(@PathVariable long id) {
        return curves.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Curve not found: " + id));
    }

    @GetMapping("/curves/{id}/points")
    public List<CurvePoint> points(@PathVariable long id) {
        return curvePoints.pointsForCurve(id);
    }
}
