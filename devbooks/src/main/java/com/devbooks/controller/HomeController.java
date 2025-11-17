package com.devbooks.controller;

import com.devbooks.entity.Book;
import com.devbooks.entity.Category;
import com.devbooks.service.BookService;
import com.devbooks.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class HomeController {

    @Autowired
    private BookService bookService;

    @Autowired
    private CategoryService categoryService;

    /**
     * Xử lý trang chủ
     */
    @GetMapping("/")
    public String home(Model model) {
        List<Book> newestBooks = bookService.getNewestBooks();
        List<Book> topSellingBooks = bookService.getTopSellingBooks();
        List<Category> categoryList = categoryService.getAllCategories();

        model.addAttribute("newestBooks", newestBooks);
        model.addAttribute("topSellingBooks", topSellingBooks);
        model.addAttribute("categories", categoryList);

        model.addAttribute("activePage", "home");

        return "user/index"; // Đảm bảo file index.html nằm trong templates/user/
    }

    /**
     * Xử lý trang "Sản phẩm"
     */
    @GetMapping("/products")
    public String showAllProducts(
            Model model,
            @RequestParam(defaultValue = "0") int page
    ) {
        Pageable pageable = PageRequest.of(page, 12);
        Page<Book> bookPage = bookService.getAllBooks(pageable);

        int totalPages = bookPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("bookPage", bookPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("activeCategory", null);

        model.addAttribute("activePage", "products");

        return "user/products";
    }

    /**
     * ✅ HÀM MỚI: Hiển thị trang Giới Thiệu
     * URL: GET /about
     */
    @GetMapping("/about")
    public String showAboutPage(Model model) {
        model.addAttribute("activePage", "about"); // Kích hoạt Navbar
        return "user/about"; // Trả về file templates/user/about.html
    }

    /**
     * ✅ HÀM MỚI: Hiển thị trang Liên Hệ
     * URL: GET /contact
     */
    @GetMapping("/contact")
    public String showContactPage(Model model) {
        model.addAttribute("activePage", "contact"); // Kích hoạt Navbar
        return "user/contact"; // Trả về file templates/user/contact.html
    }

    /**
     * ✅ HÀM ĐÃ ĐƯỢC KHÔI PHỤC
     * Xử lý trang chi tiết sách
     */
    @GetMapping("/book/{id}")
    public String bookDetail(@PathVariable("id") Long id, Model model) {
        // 1. Tìm sách trong DB
        Book book = bookService.getBookById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid book Id:" + id));

        // 2. Thêm sách vào Model
        model.addAttribute("book", book);

        model.addAttribute("activePage", "products");

        // 3. Trả về file HTML
        return "user/book-detail";
    }

    /**
     * Xử lý lọc sách theo danh mục
     */
    @GetMapping("/category/{id}")
    public String booksByCategory(
            @PathVariable("id") Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, 12);
        Page<Book> bookPage = bookService.getBooksByCategoryId(categoryId, pageable);

        int totalPages = bookPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("bookPage", bookPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("activeCategory", categoryId);

        model.addAttribute("activePage", "products");

        return "user/products";
    }

    /**
     * Xử lý tìm kiếm sách
     */
    @GetMapping("/search")
    public String searchBooks(
            @RequestParam("keyword") String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, 12);
        Page<Book> bookPage = bookService.searchBooks(keyword, pageable);

        int totalPages = bookPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("bookPage", bookPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("searchKeyword", keyword);

        model.addAttribute("activePage", "products");

        return "user/products";
    }
}