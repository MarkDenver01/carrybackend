package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.model.entity.Product;
import com.carry_guide.carry_guide_admin.model.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDateTime;


@Repository
public interface JpaProductRepository extends JpaRepository<Product, Long> {

    // 🔹 Get latest products (used in homepage)
    List<Product> findTop20ByOrderByProductIdDesc();

    // 🔹 Find product by primary productId
    Product findByProductId(Long productId);

    // 🔹 Filter by status ("Available", "Out of Stock", etc.)
    List<Product> findByProductStatus(String productStatus);

    // 🔹 Search matching product name — used in AI keyword searching
    List<Product> findByProductNameContainingIgnoreCase(String keyword);

    // 🔹 Search by category name
    List<Product> findByCategory_CategoryNameContainingIgnoreCase(String keyword);

    // 🔹 Get products by category object
    List<Product> findByCategory(ProductCategory category);


    // ==========================
    //   🔥 AI-SPECIFIC QUERIES
    // ==========================

    // 🔹 Show only AVAILABLE products
    @Query("""
                SELECT p
                FROM Product p
                WHERE p.productStatus = 'Available'
                ORDER BY p.productInDate DESC
            """)
    List<Product> findAllActiveProducts(); // “Active” = available products


    // 🔹 Used when GPT returns a sorted list of product IDs
    @Query("""
                SELECT p 
                FROM Product p
                WHERE p.productId IN :ids
            """)
    List<Product> findByProductIds(List<Long> ids);


    @Query("""
                SELECT p
                FROM Product p
                WHERE p.productStatus = 'Available'
                  AND (
                      LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                      OR LOWER(p.productDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
            """)
    List<Product> searchByKeyword(String keyword);

    @Query("""
                SELECT p FROM Product p
                WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :term, '%'))
                   OR LOWER(p.productDescription) LIKE LOWER(CONCAT('%', :term, '%'))
                   OR LOWER(p.category.categoryName) LIKE LOWER(CONCAT('%', :term, '%'))
            """)
    List<Product> searchLoose(@Param("term") String term);


    // 🔹 Best seller fallback — uses "stocks" or "sold" column if you have one
    @Query("""
                SELECT p 
                FROM Product p
                WHERE p.productStatus = 'Available'
                ORDER BY p.stocks ASC
            """)
    List<Product> findBestSellers();


    // 🔹 Newest products fallback
    @Query("""
                SELECT p
                FROM Product p
                WHERE p.productStatus = 'Available'
                ORDER BY p.productInDate DESC
            """)
    List<Product> findNewestProducts();

    // ❌ OUT OF STOCK (0 stocks or status 'Out of Stock')
    // OUT OF STOCK
    @Query("""
                SELECT COUNT(p)
                FROM Product p
                WHERE p.stocks = 0
                   OR LOWER(p.productStatus) = 'out of stock'
            """)
    long countOutOfStock();

    // LOW STOCK (1–60)
    @Query("""
                SELECT COUNT(p)
                FROM Product p
                WHERE p.stocks BETWEEN 1 AND 60
            """)
    long countLowStock();

    // EXPIRING SOON — 1 to 60 days left
    @Query("""
                SELECT COUNT(p)
                FROM Product p
                WHERE p.expiryDate IS NOT NULL
                  AND DATE(p.expiryDate) BETWEEN DATE(:now) AND DATE(:limit)
            """)
    long countExpiringSoon(
            @Param("now") LocalDateTime now,
            @Param("limit") LocalDateTime limit
    );

}